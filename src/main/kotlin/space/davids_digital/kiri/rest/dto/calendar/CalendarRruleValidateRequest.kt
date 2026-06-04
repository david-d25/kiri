package space.davids_digital.kiri.rest.dto.calendar

data class CalendarRruleValidateRequest(val rrule: String)

data class CalendarRruleValidateResponse(val valid: Boolean, val error: String? = null)
