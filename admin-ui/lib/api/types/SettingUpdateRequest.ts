export type SettingUpdateRequest = {
    key: string,
    value: string | null,
    encrypt?: boolean
}