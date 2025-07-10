import {api} from "./api";
import {FrameBufferStateDto} from "./types/FrameBufferStateDto";

export function fetchFrameBuffer(): Promise<FrameBufferStateDto> {
    return api.get<FrameBufferStateDto>('/admin/agent/framebuffer');
}