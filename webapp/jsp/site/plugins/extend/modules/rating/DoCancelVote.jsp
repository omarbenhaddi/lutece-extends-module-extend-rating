<%@page import="fr.paris.lutece.portal.service.message.SiteMessageException"%>
<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>
<jsp:useBean id="ratingJspBean" scope="request" class="fr.paris.lutece.plugins.extend.modules.rating.web.RatingJspBean" />

<%
	try
	{
		ratingJspBean.doCancelVote( request, response );
	}
	catch( SiteMessageException lme )
	{
		response.sendRedirect( AppPathService.getBaseUrl( request ) + AppPathService.getPortalUrl(  ) );
	}
%>