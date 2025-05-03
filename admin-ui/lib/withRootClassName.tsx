import React from "react";

export function withRootClassName<P extends object>(WrappedComponent: React.ComponentType<P>) {
    return function ComponentWithClassName(props: P & { className?: string }) {
        const { className, ...rest } = props;
        const ComponentWrapper = (props: P) => {
            const result = <WrappedComponent {...props} />;
            if (React.isValidElement(result)) {
                return React.cloneElement(result as React.ReactElement<any>, {
                    className: `${(result.props as any).className || ''} ${className || ''}`.trim()
                });
            }
            return result;
        };
        return <ComponentWrapper {...rest as P} />;
    };
}