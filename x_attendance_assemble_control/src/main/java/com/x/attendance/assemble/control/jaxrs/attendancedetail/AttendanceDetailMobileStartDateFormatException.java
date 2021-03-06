package com.x.attendance.assemble.control.jaxrs.attendancedetail;

import com.x.base.core.exception.PromptException;

class AttendanceDetailMobileStartDateFormatException extends PromptException {

	private static final long serialVersionUID = 1859164370743532895L;

	AttendanceDetailMobileStartDateFormatException( Throwable e, String date ) {
		super("查询开始日期格式异常，格式：yyyy-mm-dd.日期：" + date, e);
	}
}
