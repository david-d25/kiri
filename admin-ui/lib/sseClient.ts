import {EngineStateDto} from "@/lib/api/types/EngineStateDto";
import {apiBase} from "@/lib/api/api";
import {FrameBufferStateDto} from "@/lib/api/types/FrameBufferStateDto";

type Handler<T = any> = (data: T) => void;

export interface EventMap {
    engineState: EngineStateDto;
    frameBufferState: FrameBufferStateDto;
    heartbeat: null;
}

export type SSEventName = keyof EventMap;

let eventSource: EventSource | null = null;
const handlers: Map<string, Set<Handler>> = new Map();
const dispatchers = new Map<string, (e: Event) => void>();

function ensureEventSource(): EventSource | null {
    if (eventSource) {
        return eventSource;
    }

    if (typeof window === "undefined") {
        return null;
    }

    eventSource = new EventSource(apiBase + "/agent/events/subscribe", { withCredentials: true } as any);

    eventSource.onerror = () => {
        // Browser will reconnect automatically
        console.warn("SSE connection error");
    };

    return eventSource;
}

export const sseClient = {
    on<K extends SSEventName>(name: K, handler: Handler<EventMap[K]>): () => void {
        ensureEventSource();
        if (!handlers.has(name as string)) {
            handlers.set(name as string, new Set());

            const dispatcher = (e: Event) => {
                const raw = (e as MessageEvent<string>).data;
                let data: any = null;
                if (raw && raw.length) {
                    try { data = JSON.parse(raw); } catch { data = raw; }
                }
                handlers.get(name as string)?.forEach(h => h(data));
            };

            dispatchers.set(name as string, dispatcher);
            eventSource!.addEventListener(name as string, dispatcher as EventListener);
        }
        handlers.get(name)!.add(handler as Handler);
        return () => {
            const set = handlers.get(name as string);
            if (!set) {
                return;
            }
            set.delete(handler as Handler);
            if (set.size === 0) {
                handlers.delete(name as string);
                const dispatcher = dispatchers.get(name as string);
                if (dispatcher && eventSource) {
                    eventSource.removeEventListener(name as string, dispatcher as EventListener);
                }
                dispatchers.delete(name as string);
            }
        };
    },
    close() {
        if (eventSource) {
            for (const [n, d] of dispatchers) {
                eventSource.removeEventListener(n, d as EventListener);
            }
            dispatchers.clear();
            eventSource.close();
            eventSource = null;
        }
        handlers.clear();
    }
};