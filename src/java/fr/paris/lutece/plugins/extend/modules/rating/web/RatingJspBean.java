/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.extend.modules.rating.web;

import fr.paris.lutece.plugins.extend.business.extender.history.ResourceExtenderHistory;
import fr.paris.lutece.plugins.extend.business.extender.history.ResourceExtenderHistoryFilter;
import fr.paris.lutece.plugins.extend.modules.rating.business.Rating;
import fr.paris.lutece.plugins.extend.modules.rating.business.RatingHistory;
import fr.paris.lutece.plugins.extend.modules.rating.business.config.RatingExtenderConfig;
import fr.paris.lutece.plugins.extend.modules.rating.service.IRatingHistoryService;
import fr.paris.lutece.plugins.extend.modules.rating.service.IRatingService;
import fr.paris.lutece.plugins.extend.modules.rating.service.RatingHistoryService;
import fr.paris.lutece.plugins.extend.modules.rating.service.RatingService;
import fr.paris.lutece.plugins.extend.modules.rating.service.extender.RatingResourceExtender;
import fr.paris.lutece.plugins.extend.modules.rating.service.security.IRatingSecurityService;
import fr.paris.lutece.plugins.extend.modules.rating.service.security.RatingSecurityService;
import fr.paris.lutece.plugins.extend.modules.rating.util.constants.RatingConstants;
import fr.paris.lutece.plugins.extend.service.ExtendPlugin;
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.extender.ResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.extender.config.IResourceExtenderConfigService;
import fr.paris.lutece.plugins.extend.service.extender.history.IResourceExtenderHistoryService;
import fr.paris.lutece.plugins.extend.service.extender.history.ResourceExtenderHistoryService;
import fr.paris.lutece.portal.business.mailinglist.Recipient;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.mailinglist.AdminMailingListService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;


/**
 * 
 * RatingJspBean
 * 
 */
public class RatingJspBean
{
    // TEMPLATES
    private static final String TEMPLATE_RATING_NOTIFY_MESSAGE = "skin/plugins/extend/modules/rating/rating_notify_message.html";

    // SERVICES
    private IRatingService _ratingService = SpringContextService.getBean( RatingService.BEAN_SERVICE );
    private IResourceExtenderHistoryService _resourceExtenderHistoryService = SpringContextService
            .getBean( ResourceExtenderHistoryService.BEAN_SERVICE );
    private IResourceExtenderConfigService _configService = SpringContextService
            .getBean( RatingConstants.BEAN_CONFIG_SERVICE );
    private IResourceExtenderService _resourceExtenderService = SpringContextService
            .getBean( ResourceExtenderService.BEAN_SERVICE );
    private IRatingSecurityService _ratingSecurityService = SpringContextService
            .getBean( RatingSecurityService.BEAN_SERVICE );
    private IRatingHistoryService _ratingHistoryService = SpringContextService
            .getBean( RatingHistoryService.BEAN_SERVICE );

    /**
     * Update the vote value an count.
     * This method is called in FO by the following JSP :
     * <strong>jsp/site/plugins/extend/modules/rating/DoVote.Jsp</strong>
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException the io exception
     * @throws SiteMessageException the site message exception
     */
    public void doVote( HttpServletRequest request, HttpServletResponse response ) throws IOException,
            SiteMessageException
    {
        String strIdExtendableResource = request.getParameter( RatingConstants.PARAMETER_ID_EXTENDABLE_RESOURCE );
        String strExtendableResourceType = request.getParameter( RatingConstants.PARAMETER_EXTENDABLE_RESOURCE_TYPE );
        String strVoteValue = request.getParameter( RatingConstants.PARAMETER_VOTE_VALUE );
        String strFromUrl = (String) request.getSession( ).getAttribute(
                ExtendPlugin.PLUGIN_NAME + RatingConstants.PARAMETER_FROM_URL );
        if ( StringUtils.isBlank( strIdExtendableResource ) || StringUtils.isBlank( strExtendableResourceType )
                || StringUtils.isBlank( strVoteValue ) )
        {
            SiteMessageService.setMessage( request, RatingConstants.MESSAGE_ERROR_GENERIC_MESSAGE,
                    SiteMessage.TYPE_STOP );
        }

        // Check if the user can vote or not
        if ( !_ratingSecurityService.canVote( request, strIdExtendableResource, strExtendableResourceType ) )
        {
            SiteMessageService.setMessage( request, RatingConstants.MESSAGE_CANNOT_VOTE, SiteMessage.TYPE_STOP );
        }

        int nVoteValue = 0;

        try
        {
            nVoteValue = Integer.parseInt( strVoteValue );
        }
        catch ( NumberFormatException e )
        {
            SiteMessageService.setMessage( request, RatingConstants.MESSAGE_ERROR_GENERIC_MESSAGE,
                    SiteMessage.TYPE_STOP );
        }

        _ratingService.doVote( strIdExtendableResource, strExtendableResourceType, nVoteValue, request );

        sendNotification( request, strIdExtendableResource, strExtendableResourceType, nVoteValue );

        String strReferer = request.getHeader( RatingConstants.PARAMETER_HTTP_REFERER );

        if ( strReferer != null )
        {
            UrlItem url = new UrlItem( strReferer );
            if ( StringUtils.isNotEmpty( strFromUrl ) )
            {
                strFromUrl = strFromUrl.replaceAll( "%", "%25" );
                url.addParameter( RatingConstants.PARAMETER_FROM_URL, strFromUrl );
            }
            response.sendRedirect( url.getUrl( ) );
        }
        else
        {
            response.sendRedirect( AppPathService.getPortalUrl( ) );
        }
    }

    /**
     * Cancel the vote value
     * This method is called in FO by the following JSP :
     * <strong>jsp/site/plugins/extend/modules/rating/DoCancelVote.Jsp</strong>
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException the io exception
     * @throws SiteMessageException the site message exception
     */
    public void doCancelVote( HttpServletRequest request, HttpServletResponse response ) throws IOException,
            SiteMessageException
    {
        String strIdExtendableResource = request.getParameter( RatingConstants.PARAMETER_ID_EXTENDABLE_RESOURCE );
        String strExtendableResourceType = request.getParameter( RatingConstants.PARAMETER_EXTENDABLE_RESOURCE_TYPE );
        String strFromUrl = (String) request.getSession( ).getAttribute(
                ExtendPlugin.PLUGIN_NAME + RatingConstants.PARAMETER_FROM_URL );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        if ( StringUtils.isBlank( strIdExtendableResource ) || StringUtils.isBlank( strExtendableResourceType ) )
        {
            SiteMessageService.setMessage( request, RatingConstants.MESSAGE_ERROR_GENERIC_MESSAGE,
                    SiteMessage.TYPE_STOP );
        }

        // Check if the user can vote or not
        if ( !_ratingSecurityService.canDeleteVote( request, strIdExtendableResource, strExtendableResourceType ) )
        {
            SiteMessageService.setMessage( request, RatingConstants.MESSAGE_CANNOT_VOTE, SiteMessage.TYPE_STOP );
        }

        ResourceExtenderHistoryFilter resourceExtenderHistoryFilter = new ResourceExtenderHistoryFilter( );
        resourceExtenderHistoryFilter.setUserGuid( user.getName( ) );
        resourceExtenderHistoryFilter.setIdExtendableResource( strIdExtendableResource );
        List<ResourceExtenderHistory> histories = _resourceExtenderHistoryService
                .findByFilter( resourceExtenderHistoryFilter );

        if ( CollectionUtils.isNotEmpty( histories ) )
        {
            ResourceExtenderHistory history = histories.get( 0 );
            RatingHistory ratingHistory = _ratingHistoryService.findByHistoryExtenderId( history.getIdHistory( ) );

            _ratingHistoryService.remove( ratingHistory.getIdRatingHistory( ) );
            _resourceExtenderHistoryService.remove( Integer.valueOf( "" + history.getIdHistory( ) ) );

            Rating rating = _ratingService.findByResource( strIdExtendableResource, strExtendableResourceType );
            rating.setVoteCount( rating.getVoteCount( ) - 1 );
            rating.setScoreValue( rating.getScoreValue( ) - ratingHistory.getVoteValue( ) );
            _ratingService.update( rating );
        }

        String strReferer = request.getHeader( RatingConstants.PARAMETER_HTTP_REFERER );

        if ( strReferer != null )
        {
            UrlItem url = new UrlItem( strReferer );
            if ( StringUtils.isNotEmpty( strFromUrl ) )
            {
                strFromUrl = strFromUrl.replaceAll( "%", "%25" );
                url.addParameter( RatingConstants.PARAMETER_FROM_URL, strFromUrl );
            }
            response.sendRedirect( url.getUrl( ) );
        }
        else
        {
            response.sendRedirect( AppPathService.getPortalUrl( ) );
        }
    }

    /**
     * Send notification.
     * @param request the request
     * @param strIdExtendableResource the str id extendable resource
     * @param strExtendableResourceType the str extendable resource type
     * @param nVoteValue the n vote value
     */
    private void sendNotification( HttpServletRequest request, String strIdExtendableResource,
            String strExtendableResourceType, int nVoteValue )
    {
        RatingExtenderConfig config = _configService.find( RatingResourceExtender.RESOURCE_EXTENDER,
                strIdExtendableResource, strExtendableResourceType );
        int nMailingListId = config.getIdMailingList( );
        Collection<Recipient> listRecipients = AdminMailingListService.getRecipients( nMailingListId );

        for ( Recipient recipient : listRecipients )
        {
            Map<String, Object> model = new HashMap<String, Object>( );

            String strSenderName = AppPropertiesService.getProperty( RatingConstants.PROPERTY_LUTECE_NAME );
            String strSenderEmail = AppPropertiesService.getProperty( RatingConstants.PROPERTY_WEBMASTER_EMAIL );
            String strResourceName = _resourceExtenderService.getExtendableResourceName( strIdExtendableResource,
                    strExtendableResourceType );

            Object[] params = { strResourceName };
            String strSubject = I18nService.getLocalizedString( RatingConstants.MESSAGE_NOTIFY_SUBJECT, params,
                    request.getLocale( ) );

            model.put( RatingConstants.MARK_RESOURCE_EXTENDER_NAME, strResourceName );
            model.put( RatingConstants.MARK_VOTE_VALUE, nVoteValue );

            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_RATING_NOTIFY_MESSAGE,
                    request.getLocale( ), model );
            String strBody = template.getHtml( );

            MailService.sendMailHtml( recipient.getEmail( ), strSenderName, strSenderEmail, strSubject, strBody );
        }
    }
}
