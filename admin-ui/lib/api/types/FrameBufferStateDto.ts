import {DataFrameDto, FrameDto} from "./FrameDto";

export type FrameBufferStateDto = {
    fixedFrames: DataFrameDto[],
    rollingFrames: FrameDto[],
    hardLimit: number
};