import React from "react";
import styles from './SkeletonLoader.module.scss';
import {withRootClassName} from "../../lib/withRootClassName";

type SkeletonType = 'text' | 'circle' | 'rectangle' | 'card';

type Props = {
    type?: SkeletonType;
    width?: number | string;
    height?: number | string;
    borderRadius?: number | string;
    animation?: 'pulse' | 'wave' | 'none';
    count?: number;
};

function SkeletonLoader(
    {
        type = 'text',
        width,
        height,
        borderRadius,
        animation = 'pulse',
        count = 1
    }: Props
) {
    const getTypeStyles = (): React.CSSProperties => {
        switch (type) {
            case 'text':
                return {
                    width: width || '100%',
                    height: height || '16px',
                    borderRadius: borderRadius || '4px',
                    marginBottom: '8px'
                };
            case 'circle':
                const size = width || height || '48px';
                return {
                    width: size,
                    height: size,
                    borderRadius: '50%'
                };
            case 'rectangle':
                return {
                    width: width || '100%',
                    height: height || '80px',
                    borderRadius: borderRadius || '4px'
                };
            case 'card':
                return {
                    width: width || '300px',
                    height: height || '200px',
                    borderRadius: borderRadius || '10px'
                };
            default:
                return {};
        }
    };

    const renderSkeletons = () => {
        const skeletons = [];

        for (let i = 0; i < count; i++) {
            skeletons.push(
                <div
                    key={i}
                    className={`${styles.skeleton} ${styles[`animation-${animation}`]}`}
                    style={getTypeStyles()}
                    data-type={type}
                />
            );
        }

        return skeletons;
    };

    return <>{renderSkeletons()}</>;
}

export default withRootClassName(SkeletonLoader);