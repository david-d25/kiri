import React, { useState } from "react";
import styles from './UserAvatar.module.scss';

type Props = {
    src?: string;
    alt?: string;
    size?: 'small' | 'medium' | 'large';
    name?: string;
    backgroundColor?: string;
    textColor?: string;
};

export default function UserAvatar(
    {
        src,
        alt = 'User avatar',
        size = 'medium',
        name,
        backgroundColor,
        textColor = 'white'
    }: Props
) {
    const [imageError, setImageError] = useState(false);

    const hasImage = src && !imageError;

    // Generate initials from name (up to 2 letters)
    const getInitials = () => {
        if (!name) return '?';

        const parts = name.trim().split(/\s+/);
        if (parts.length === 1) {
            return name.charAt(0).toUpperCase();
        }

        return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    };

    // Generate random background color based on name
    const getBackgroundColor = () => {
        if (backgroundColor) return backgroundColor;

        if (!name) return '#757575'; // Default gray

        // Generate a consistent color based on the name
        let hash = 0;
        for (let i = 0; i < name.length; i++) {
            hash = name.charCodeAt(i) + ((hash << 5) - hash);
        }

        const colors = [
            '#e57373', '#f06292', '#ba68c8', '#9575cd',
            '#7986cb', '#64b5f6', '#4fc3f7', '#4dd0e1',
            '#4db6ac', '#81c784', '#aed581', '#dce775',
            '#fff176', '#ffd54f', '#ffb74d', '#ff8a65'
        ];

        const index = Math.abs(hash) % colors.length;
        return colors[index];
    };

    const handleImageError = () => {
        setImageError(true);
    };

    return (
        <div
            className={`${styles.avatar} ${styles[size]}`}
            style={{ backgroundColor: !hasImage ? getBackgroundColor() : undefined }}
        >
            {hasImage ? (
                <img
                    src={src}
                    alt={alt}
                    className={styles.image}
                    onError={handleImageError}
                />
            ) : (
                <span
                    className={styles.initials}
                    style={{ color: textColor }}
                >
                    {getInitials()}
                </span>
            )}
        </div>
    );
}