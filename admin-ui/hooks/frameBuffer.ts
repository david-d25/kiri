import {useFetch} from "./apiHooks";
import {FrameBufferStateDto} from "../lib/api/types/FrameBufferStateDto";

export function useFrameBuffer() {
    return useFetch<FrameBufferStateDto>('frameBuffer', '/admin/agent/framebuffer');
}