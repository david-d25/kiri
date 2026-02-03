import {FrameDto} from "./FrameDto";

export type FrameBufferStateDto = {
    frames: FrameDto[],
    hardLimit: number
};

export namespace FrameBufferStateDto {
    export const NULL: FrameBufferStateDto = {
        frames: [],
        hardLimit: 0
    };
}