import {
    ContentPartDto,
    DataFrameDto, FindInPageDto,
    FrameDto, NativeWebSearchFrameDto, OpenPageDto, SearchDto,
    ToolCallFrameDto,
    ToolInputDto,
    ToolOutputDto
} from "@/lib/api/types/FrameDto";

import BracketsLiteralIcon from "@/icons/brackets-literal.svg";
import FunctionIcon from "@/icons/function.svg";
import GlobeIcon from "@/icons/globe.svg";

import s from './Frame.module.scss';
import {classnames} from "@/lib/classnames";
import {truncateWithEllipsis} from "@/lib/stringUtils";

type Props = {
    frame: FrameDto
};

export default function Frame(props: Props) {
    const { frame } = props;
    switch (frame.type) {
        case "data":
            return <DataFrame frame={frame}/>
        case "toolCall":
            return <ToolCallFrame frame={frame}/>
        case "nativeWebSearch":
            return <NativeWebSearchFrame frame={frame}/>
    }
}

function NativeWebSearchFrame(props: { frame: NativeWebSearchFrameDto }) {
    const webSearchFrame = props.frame.webSearch;
    switch (webSearchFrame.type) {
        case "search":
            return <SearchFrame frame={webSearchFrame}/>;
        case "findInPage":
            return <FindInPageFrame frame={webSearchFrame}/>;
        case "openPage":
            return <OpenPageFrame frame={webSearchFrame}/>;
    }
}

function SearchFrame(props: { frame: SearchDto }) {
    return (
        <div className={s.frame}>
            <div className={s.head}>
                <GlobeIcon className={classnames(s.icon, s.data)}/>
                <div className={s.tag}>
                    Search: "{props.frame.query}"
                </div>
            </div>
        </div>
    );
}

function OpenPageFrame(props: { frame: OpenPageDto }) {
    return (
        <div className={s.frame}>
            <div className={s.head}>
                <GlobeIcon className={classnames(s.icon, s.data)}/>
                <div className={s.tag}>
                    Open Page: {props.frame.url}
                </div>
            </div>
        </div>
    );
}

function FindInPageFrame(props: { frame: FindInPageDto }) {
    return (
        <div className={s.frame}>
            <div className={s.head}>
                <GlobeIcon className={classnames(s.icon, s.data)}/>
                <div className={s.tag}>
                    Find In Page: "{props.frame.pattern}" in {props.frame.url}
                </div>
            </div>
        </div>
    );
}

function DataFrame(props: { frame: DataFrameDto }) {
    return (
        <div className={s.frame}>
            <div className={s.head}>
                <BracketsLiteralIcon className={classnames(s.icon, s.data)}/>
                <div className={s.tag}>
                    {props.frame.tag}
                </div>
                <DataFrameAttributes attributes={props.frame.attributes}/>
            </div>
            <div className={s.content}>
                <ContentList content={props.frame.content}/>
            </div>
        </div>
    );
}

function DataFrameAttributes({ attributes }: { attributes: { [_: string]: string } }) {
    const keys = Object.keys(attributes);
    return (
        <span className={s.toolCallInput} data-type={'object'}>
            {
                keys.map((key, index) => {
                    return (
                        <span className={s.keyValuePair}>
                            <span>{key}</span>
                            <span>=</span>
                            <span>"{attributes[key]}"</span>
                            { index < keys.length - 1 && <span>, </span>}
                        </span>
                    )
                })
            }
        </span>
    )
}

function ToolCallFrame(props: { frame: ToolCallFrameDto }) {
    return (
        <div className={s.frame}>
            <div className={s.head}>
                <FunctionIcon className={classnames(s.icon, s.function)}/>
                <span className={s.tag}>
                    {props.frame.toolUse.name}
                </span>
                <ToolCallInput input={props.frame.toolUse.input}/>
            </div>
            <div className={s.content}>
                { props.frame.result.output.map((part, index) => (
                    <ToolOutput key={index} toolOutput={part}/>
                ) ) }
            </div>
        </div>
    );
}

function ToolCallInput({ input }: { input: ToolInputDto }) {
    if (input.type == "text") {
        return (
            <span className={s.toolCallInput} data-type={'text'} title={input.text}>
                "{truncateWithEllipsis(input.text, 32)}"
            </span>
        )
    } else if (input.type == "number") {
        return (
            <span className={s.toolCallInput} data-type={'number'}>
                {input.number}
            </span>
        )
    } else if (input.type == "boolean") {
        return (
            <span className={s.toolCallInput} data-type={'boolean'}>
                {input.boolean}
            </span>
        )
    } else if (input.type == "array") {
        return (
            <span className={s.toolCallInput} data-type={'array'}>
                <span>[ </span>
                {
                    input.items.map((item, index) => {
                        return (
                            <>
                                { index > 0 && <span>, </span> }
                                <ToolCallInput key={index} input={item}/>
                            </>
                        );
                    })
                }
                <span> ]</span>
            </span>
        )
    } else if (input.type == "object") {
        const keys = Object.keys(input.items);
        return (
            <span className={s.toolCallInput} data-type={'object'}>
                <span>{'{ '}</span>
                {
                    keys.map((key, index) => {
                        return (
                            <span className={s.keyValuePair}>
                                <span>{key}</span>
                                <span>: </span>
                                <span><ToolCallInput input={input.items[key]}/></span>
                                { index < keys.length - 1 && <span>, </span>}
                            </span>
                        )
                    } )
                }
                <span>{' }'}</span>
            </span>
        )
    }
    return <span className={s.toolCallInput} data-type={'unknown'}>(unknown data)</span>
}

function ToolOutput({ toolOutput }: { toolOutput: ToolOutputDto }) {
    if (toolOutput.type == "text") {
        return (
            <span className={s.toolOutputText}>
                {toolOutput.text.split('\n').map(line => <><span>{line}</span><br/></>)}
            </span>
        )
    }
    if (toolOutput.type == "image") {
        const { imageType, base64 } = toolOutput;
        return (
            <img className={s.toolOutputImage} src={`data:${imageType};base64,${base64}`} alt="Image"/>
        )
    }
    return <span>[!] Unknown content part</span>;
}

function ContentList(props: { content: ContentPartDto[] }) {
    return (
        <span className={s.contentList}>
            { props.content.map((part, index) => (
                <ContentPart key={index} part={part}/>
            )) }
        </span>
    )
}

function ContentPart(props: { part: ContentPartDto }) {
    const { part } = props;
    if (part.type === 'text') {
        return <span className={s.textContentPart}>
            {
                part.text.split('\n').map((line, index) => {
                    return (
                        <>
                            { index > 0 && <br/> }
                            <span>{line}</span>
                        </>
                    )
                })
            }
        </span>;
    } else if (part.type === 'image') {
        return (
            <>
                <img className={s.textContentImage} src={`data:${part.imageType};base64,${part.base64}`} alt="Image"/>
                <br/>
            </>
        );
    }
    return <span>[!] Unknown content part</span>;
}
