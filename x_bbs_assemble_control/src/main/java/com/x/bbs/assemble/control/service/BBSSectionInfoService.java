package com.x.bbs.assemble.control.service;

import java.util.List;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.entity.annotation.CheckRemoveType;
import com.x.base.core.logger.Logger;
import com.x.base.core.logger.LoggerFactory;
import com.x.bbs.assemble.control.Business;
import com.x.bbs.entity.BBSForumInfo;
import com.x.bbs.entity.BBSPermissionInfo;
import com.x.bbs.entity.BBSPermissionRole;
import com.x.bbs.entity.BBSRoleInfo;
import com.x.bbs.entity.BBSSectionInfo;
import com.x.bbs.entity.BBSUserRole;
import com.x.organization.core.express.wrap.WrapDepartment;

/**
 * 论坛信息管理服务类
 * @author LIYI
 *
 */
public class BBSSectionInfoService {
	
	private Logger logger = LoggerFactory.getLogger( BBSSectionInfoService.class );
	private BBSSubjectInfoService subjectInfoService = new BBSSubjectInfoService();
	
	/**
	 * 根据传入的ID从数据库查询BBSSectionInfo对象
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public BBSSectionInfo get( String id ) throws Exception {
		if( id  == null || id.isEmpty() ){
			throw new Exception( "id is null!" );
		}
		try ( EntityManagerContainer emc = EntityManagerContainerFactory.instance().create() ) {
			return emc.find( id, BBSSectionInfo.class );
		}catch( Exception e ){
			throw e;
		}
	}
	
	/**
	 * 向数据库保存BBSSectionInfo对象
	 * @param wrapIn
	 */
	public BBSSectionInfo save( EntityManagerContainer emc, BBSSectionInfo _bBSSectionInfo ) throws Exception {
		BBSSectionInfo _bBSSectionInfo_tmp = null;
		BBSForumInfo _forumInfo_tmp = null;
		if( _bBSSectionInfo.getId() == null ){
			_bBSSectionInfo.setId( BBSSectionInfo.createId() );
		}
		_bBSSectionInfo_tmp = emc.find( _bBSSectionInfo.getId(), BBSSectionInfo.class );
		_forumInfo_tmp = emc.find( _bBSSectionInfo.getForumId(), BBSForumInfo.class );
		emc.beginTransaction( BBSForumInfo.class );
		emc.beginTransaction( BBSSectionInfo.class );
		if( _bBSSectionInfo_tmp == null ){
			emc.persist( _bBSSectionInfo, CheckPersistType.all);
			if( _forumInfo_tmp != null ){
				_forumInfo_tmp.setSectionTotal( _forumInfo_tmp.getSectionTotal() + 1 );
				emc.check( _forumInfo_tmp, CheckPersistType.all );	
			}
		}else{
			_bBSSectionInfo.copyTo( _bBSSectionInfo_tmp );
			emc.check( _bBSSectionInfo_tmp, CheckPersistType.all );
		}
		emc.commit();
		return _bBSSectionInfo;
	}
	
	/**
	 * 根据ID从数据库中删除BBSSectionInfo对象
	 * @param id
	 * @throws Exception
	 */
	public void delete( EntityManagerContainer emc, String id ) throws Exception {	
		if( id == null || id.isEmpty() ){
			throw new Exception( "id is null, system can not delete any object." );
		}
		BBSSectionInfo bBSSectionInfo = null;
		BBSForumInfo forumInfo = null;
		Business business = null;
		List<String> subSectionIds = null;
		List<String> subjectIds = null;
		List<BBSPermissionInfo> permissionList = null;
		List<BBSPermissionRole> permissionRoleList = null;
		business = new Business( emc );
		
		emc.beginTransaction( BBSPermissionInfo.class );
		emc.beginTransaction( BBSPermissionRole.class );
		//递归删除所有的子版块
		subSectionIds = business.sectionInfoFactory().listSubSectionIdsByMainSectionId( id );
		if( subSectionIds != null && !subSectionIds.isEmpty() ){
			for( String subSectionId : subSectionIds ){
				if( !id.equalsIgnoreCase( subSectionId )){
					delete( emc, subSectionId );
				}
			}
		}
		//删除所有的权限以及角色权限关联
		permissionList = business.permissionInfoFactory().listPermissionByMainSectionId( id );
		if( permissionList != null && !permissionList.isEmpty() ){
			for( BBSPermissionInfo permissionInfo : permissionList ){
				//删除角色权限关联
				permissionRoleList = business.permissionRoleFactory().listByPermissionCode( permissionInfo.getPermissionCode() );
				if( permissionRoleList != null && permissionRoleList.size() > 0 ){
					for( BBSPermissionRole permissionRole : permissionRoleList ){
						emc.remove( permissionRole, CheckRemoveType.all );
					}
				}
				emc.remove( permissionInfo, CheckRemoveType.all );
			}
		}
		//删除主版块以及子版块所有的主贴和回复, 一次删除1000条
		subjectIds = business.subjectInfoFactory().listSubjectIdsBySection( id, 1000 );
		do{
			if( subjectIds != null && !subjectIds.isEmpty() ){
				for( String subjectId : subjectIds ){
					subjectInfoService.delete( emc, subjectId );
				}
			}
			subjectIds = business.subjectInfoFactory().listSubjectIdsBySection( id, 1000 );
		}while( subjectIds != null && !subjectIds.isEmpty() );
		
		bBSSectionInfo = emc.find(id, BBSSectionInfo.class);
		if ( null != bBSSectionInfo ) {
			emc.beginTransaction( BBSForumInfo.class );
			emc.beginTransaction( BBSSectionInfo.class );
			forumInfo = emc.find( bBSSectionInfo.getForumId(), BBSForumInfo.class );
			if( forumInfo != null ){//论坛版块数 -1
				if( forumInfo.getSectionTotal() > 0 ){//论坛版块数 -1
					forumInfo.setSectionTotal( forumInfo.getSectionTotal() - 1 );
				}else{
					forumInfo.setSectionTotal( 0L );
				}
			}
			emc.check( forumInfo, CheckPersistType.all );
			emc.remove( bBSSectionInfo, CheckRemoveType.all );
		}
		emc.commit();
	}

	public List<BBSSectionInfo> listAll( EntityManagerContainer emc ) throws Exception {
		Business business = new Business( emc );
		return business.sectionInfoFactory().listAll();
	}
	
	public List<String> listByForumId( EntityManagerContainer emc, String forumId ) throws Exception {
		if( forumId  == null || forumId.isEmpty() ){
			throw new Exception( "forumId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().listByForumId( forumId );
	}

	/**
	 * 根据论坛ID查询论坛中所有的主版块信息数量
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public Long countMainSectionByForumId( EntityManagerContainer emc, String forumId ) throws Exception {
		if( forumId  == null || forumId.isEmpty() ){
			throw new Exception( "forumId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().countMainSectionByForumId( forumId );
	}
	
	/**
	 * 根据主版块ID查询主版块中所有的子版块信息数量
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public Long countSubSectionByMainSectionId( EntityManagerContainer emc, String sectionId ) throws Exception {
		if( sectionId  == null || sectionId.isEmpty() ){
			throw new Exception( "sectionId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().countSubSectionByMainSectionId( sectionId );
	}

	/**
	 * 根据论坛ID查询论坛中所有的版块信息
	 * @param forumId
	 * @return
	 * @throws Exception 
	 */
	public List<BBSSectionInfo> listMainSectionByForumId( EntityManagerContainer emc, String forumId ) throws Exception {
		if( forumId  == null || forumId.isEmpty() ){
			throw new Exception( "forumId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().listMainSectionByForumId( forumId );
	}
	
	/**
	 * 根据论坛ID查询论坛中所有的版块信息
	 * @param forumId
	 * @return
	 * @throws Exception 
	 */
	public List<BBSSectionInfo> viewMainSectionByForumId(  EntityManagerContainer emc, String forumId, List<String> viewableSectionIds ) throws Exception {
		if( forumId  == null || forumId.isEmpty() ){
			throw new Exception( "forumId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().viewMainSectionByForumId( forumId, viewableSectionIds );
	}
	
	/**
	 * 根据主版块ID查询所有的子版块信息列表
	 * @param sectionId
	 * @return
	 * @throws Exception 
	 */
	public List<BBSSectionInfo> viewSubSectionByMainSectionId( EntityManagerContainer emc, String sectionId, List<String> viewableSectionIds  ) throws Exception {
		if( sectionId  == null || sectionId.isEmpty() ){
			throw new Exception( "sectionId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().viewSubSectionByMainSectionId( sectionId, viewableSectionIds );
	}
	
	/**
	 * 根据主版块ID查询所有的子版块信息列表
	 * @param sectionId
	 * @return
	 * @throws Exception 
	 */
	public List<BBSSectionInfo> listSubSectionByMainSectionId( EntityManagerContainer emc, String sectionId  ) throws Exception {
		if( sectionId  == null || sectionId.isEmpty() ){
			throw new Exception( "sectionId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().listSubSectionByMainSectionId( sectionId );
	}

	public void checkSectionManager( EntityManagerContainer emc, String sectionId ) throws Exception {
		if( sectionId  == null || sectionId.isEmpty() ){
			throw new Exception( "sectionId is null!" );
		}
		BBSSectionInfo sectionInfo = emc.find( sectionId, BBSSectionInfo.class );
		checkSectionManager( emc, sectionInfo );
	}
	
	/**
	 * 检查版主的权限和角色设置
	 * @param sectionInfo
	 * @throws Exception 
	 */
	public void checkSectionManager( EntityManagerContainer emc, BBSSectionInfo sectionInfo ) throws Exception {
		if( sectionInfo == null ){
			throw new Exception( "sectionInfo is null!" );
		}
		String[] currentManagerNames = null;
		List<String> ids = null;
		List<BBSUserRole> userRoleList= null;
		List<WrapDepartment> departments = null;
		String superDepartment = null;
		BBSRoleInfo roleInfo = null;
		BBSUserRole userRole_new = null;
		Business business = null;
		Boolean exists = false;
		if( sectionInfo.getModeratorNames() != null && !sectionInfo.getModeratorNames().isEmpty() ){
			currentManagerNames = sectionInfo.getModeratorNames().split(",");
		}
		business = new Business( emc );
		emc.beginTransaction( BBSUserRole.class );
		roleInfo = business.roleInfoFactory().getRoleByCode( "SECTION_MANAGER_" + sectionInfo.getId() );
		if( roleInfo == null ){
			throw new Exception("role info{'code':'"+"SECTION_MANAGER_" + sectionInfo.getId()+"'} is not exists.");
		}
		ids = business.userRoleFactory().listIdsByRoleCode( "SECTION_MANAGER_" + sectionInfo.getId() );
		if( ids != null && !ids.isEmpty() ){
			userRoleList = business.userRoleFactory().list( ids );
		}
		if( userRoleList != null && !userRoleList.isEmpty() ){
			for( BBSUserRole userRole : userRoleList ){
				exists = false;
				if( currentManagerNames != null && currentManagerNames.length > 0 ){
					for( String name : currentManagerNames ){
						if( name.equals( userRole.getObjectName()) || name.equalsIgnoreCase( userRole.getUniqueId() )){
							exists = true;
						}
					}
				}
				if( !exists ){
					emc.remove( userRole, CheckRemoveType.all );
				}
			}
		}
		if( currentManagerNames != null && currentManagerNames.length > 0 ){
			for( String name : currentManagerNames ){
				exists = false;
				if( userRoleList != null && !userRoleList.isEmpty() ){
					for( BBSUserRole userRole : userRoleList ){
						if( name.equals( userRole.getObjectName()) || name.equalsIgnoreCase( userRole.getUniqueId() )){
							exists = true;
						}
					}
				}
				if( !exists ){
					userRole_new = new BBSUserRole();
					userRole_new.setObjectName( name );
					userRole_new.setUniqueId( name );
					userRole_new.setObjectType( "人员" ); //人员|组织|群组
					userRole_new.setRoleCode( roleInfo.getRoleCode() );
					userRole_new.setRoleId( roleInfo.getId() );
					userRole_new.setRoleName( roleInfo.getRoleName() );
					departments = business.organization().department().listWithPerson( name );
					if( departments != null && !departments.isEmpty() ){
						userRole_new.setCompanyName( departments.get(0).getCompany() );
						userRole_new.setDepartmentName( departments.get(0).getName() );
						superDepartment = departments.get(0).getSuperior();
						if( superDepartment == null || superDepartment.isEmpty() ){
							superDepartment = departments.get(0).getName();
						}
						userRole_new.setOrganizationName( superDepartment );
					}						
					emc.persist( userRole_new, CheckPersistType.all );
				}
			}
		}
		emc.commit();
	}

	/**
	 * 根据用户权限和论坛ID，获取所有主版块信息ID列表
	 * @param forumId
	 * @param sectionIds 
	 * @return
	 * @throws Exception 
	 */
	public List<BBSSectionInfo> listAllViewAbleMainSectionWithUserPermission( EntityManagerContainer emc, String forumId, List<String> sectionIds ) throws Exception {
		if( forumId  == null || forumId.isEmpty() ){
			throw new Exception( "forumId is null!" );
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().viewMainSectionByForumId( forumId, sectionIds );
	}

	/**
	 * 
	 * @param viewforumIds
	 * @param publicStatus 是否公开
	 * @return
	 * @throws Exception
	 */
	public List<String> viewSectionByForumIds( EntityManagerContainer emc, List<String> viewforumIds, Boolean publicStatus ) throws Exception {
		if( viewforumIds  == null || viewforumIds.isEmpty() ){
			return null;
		}
		Business business = new Business( emc );
		return business.sectionInfoFactory().viewSectionByForumId( viewforumIds, publicStatus );
	}

	public List<BBSSectionInfo> list( EntityManagerContainer emc, List<String> publicSectionIds) throws Exception {
		Business business = new Business( emc );
		return business.sectionInfoFactory().list( publicSectionIds );
	}
}