package com.x.okr.assemble.control.jaxrs.statistic;

import com.x.base.core.exception.PromptException;

class QueryEndDateInvalidException extends PromptException {

	private static final long serialVersionUID = 1859164370743532895L;

	QueryEndDateInvalidException( Throwable e, String date ) {
		super("查询条件中结束日期不合法。Date：" + date, e );
	}
}
