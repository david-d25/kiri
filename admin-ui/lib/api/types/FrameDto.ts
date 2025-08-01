export type FrameDto = DataFrameDto | ToolCallFrameDto;

export type DataFrameDto = {
    type: "data";
    tag: string;
    attributes: { [key: string]: string };
    content: ContentPartDto[];
};

export namespace ContentPartDto {
    export type Text = {
        type: "text";
        text: string;
    };

    export type Image = {
        type: "image";
        base64: string;
        imageType: string;
    };
}

export type ContentPartDto = ContentPartDto.Text | ContentPartDto.Image;

export type ToolCallFrameDto = {
    type: "toolCall"
    toolUse: ToolUseDto;
    result: ToolResultDto;
};

export type ToolUseDto = {
    id: string;
    name: string;
    input: ToolInputDto;
}

export type ToolInputDto =
    | ToolInputDto.Text
    | ToolInputDto.Number
    | ToolInputDto.BooleanVal
    | ToolInputDto.Array
    | ToolInputDto.Object;

export namespace ToolInputDto {
    export type Text = {        type: "text";       text: string                            };
    export type Number = {      type: "number";     number: number                          };
    export type BooleanVal = {  type: "boolean";    boolean: boolean                        };
    export type Array = {       type: "array";      items: ToolInputDto[]                   };
    export type Object = {      type: "object";     items: { [key: string]: ToolInputDto }  };
}

export type ToolResultDto = {
    toolUseId: string;
    name: string;
    output: ToolOutputDto;
}

export namespace ToolOutputDto {
    export type Text = {
        type: "text";
        text: string
    };
    export type Image = {
        type: "image";
        base64: string;
        imageType: string
    };
}

export type ToolOutputDto = ToolOutputDto.Text | ToolOutputDto.Image;
