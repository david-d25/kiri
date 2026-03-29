import {ToolInputDto} from "./FrameDto";

export type ToolDto = {
    fullName: string;
    description: string | null;
    parameters: ToolParameterValueDto;
};

export type ToolParameterValueDto =
    | ToolParameterObjectValue
    | ToolParameterArrayValue
    | ToolParameterStringValue
    | ToolParameterNumberValue
    | ToolParameterBooleanValue;

export type ToolParameterObjectValue = {
    type: "object";
    description: string | null;
    properties: { [key: string]: ToolParameterValueDto };
    required: string[];
};

export type ToolParameterArrayValue = {
    type: "array";
    description: string | null;
    items: ToolParameterValueDto;
};

export type ToolParameterStringValue = {
    type: "string";
    description: string | null;
    enum: string[] | null;
};

export type ToolParameterNumberValue = {
    type: "number";
    description: string | null;
};

export type ToolParameterBooleanValue = {
    type: "boolean";
    description: string | null;
};

export type ToolExecuteRequest = {
    toolName: string;
    input: ToolInputDto;
};
