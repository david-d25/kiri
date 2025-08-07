export type SettingUpdateRequestDto = {
    key: string,
    value: string | null,
    encrypt?: boolean
}