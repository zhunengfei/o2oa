package com.x.processplatform.assemble.designer.wrapout;

import java.util.ArrayList;
import java.util.List;

import com.x.base.core.entity.JpaObject;
import com.x.base.core.http.annotation.Wrap;
import com.x.processplatform.core.entity.element.Form;
import com.x.processplatform.core.entity.element.FormField;

@Wrap(FormField.class)
public class WrapOutFormField extends Form {

	private static final long serialVersionUID = -3041412588191150480L;
	public static List<String> Excludes = new ArrayList<>(JpaObject.FieldsInvisible);

}
