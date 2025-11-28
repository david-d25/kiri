import {SettingUpdateRequest} from "@/lib/api/types/SettingUpdateRequest";

export type SettingsUpdateRequest = {
    updates: { [key: string ]: SettingUpdateRequest }
}