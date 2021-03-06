package com.x.processplatform.assemble.surface.factory.element;

import java.util.List;

import com.x.base.core.exception.ExceptionWhen;
import com.x.processplatform.assemble.surface.Business;
import com.x.processplatform.core.entity.element.Merge;
import com.x.processplatform.core.entity.element.Process;

public class MergeFactory extends ElementFactory {

	public MergeFactory(Business abstractBusiness) throws Exception {
		super(abstractBusiness);
	}

	public Merge pick(String flag) throws Exception {
		return this.pick(flag, ExceptionWhen.none);
	}

	public Merge pick(String flag, ExceptionWhen exceptionWhen) throws Exception {
		return this.pick(flag, Merge.class, exceptionWhen, Merge.FLAGS);
	}

	public List<Merge> listWithProcess(Process process) throws Exception {
		List<Merge> list = this.listWithProcess(Merge.class, process);
		return list;
	}
}