package com.x.processplatform.assemble.designer.jaxrs.templateform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.http.ActionResult;
import com.x.base.core.http.EffectivePerson;
import com.x.base.core.role.RoleDefinition;
import com.x.base.core.utils.SortTools;
import com.x.processplatform.assemble.designer.Business;
import com.x.processplatform.assemble.designer.Control;
import com.x.processplatform.assemble.designer.wrapout.WrapOutTemplateFormSimple;
import com.x.processplatform.core.entity.element.TemplateForm;

class ActionList extends ActionBase {

	ActionResult<Map<String, List<WrapOutTemplateFormSimple>>> execute(EffectivePerson effectivePerson)
			throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<Map<String, List<WrapOutTemplateFormSimple>>> result = new ActionResult<>();
			List<WrapOutTemplateFormSimple> wraps = new ArrayList<>();
			Business business = new Business(emc);
			List<String> ids = business.templateForm().list();
			List<TemplateForm> os = emc.list(TemplateForm.class, ids);
			wraps = simpleOutCopier.copy(os);
			Control control = new Control();
			/** 添加管理员和流程管理员删除的权限 */
			if (effectivePerson.isManager() || business.organization().role().hasAny(effectivePerson.getName(),
					RoleDefinition.ProcessPlatformManager)) {
				control.setAllowDelete(true);
			} else {
				control.setAllowDelete(false);
			}
			for (WrapOutTemplateFormSimple o : wraps) {
				o.setControl(control);
			}
			SortTools.asc(wraps, "name");
			Map<String, List<WrapOutTemplateFormSimple>> group = wraps.stream()
					.collect(Collectors.groupingBy(e -> Objects.toString(e.getCategory(), "")));
			LinkedHashMap<String, List<WrapOutTemplateFormSimple>> sort = group.entrySet().stream()
					.sorted(Map.Entry.<String, List<WrapOutTemplateFormSimple>> comparingByKey()).collect(Collectors
							.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			result.setData(sort);
			return result;
		}
	}

}
