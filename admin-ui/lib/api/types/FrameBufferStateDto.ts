import {DataFrameDto, FrameDto} from "./FrameDto";

export type FrameBufferStateDto = {
    fixedFrames: DataFrameDto[],
    rollingFrames: FrameDto[],
    hardLimit: number
};

export namespace FrameBufferStateDto {
    export const NULL: FrameBufferStateDto = {
        fixedFrames: [],
        rollingFrames: [],
        hardLimit: 0
    };
}