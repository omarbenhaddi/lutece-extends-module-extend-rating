ALTER TABLE extend_rating_config ADD COLUMN is_active INT DEFAULT 1 NOT NULL;
ALTER TABLE extend_rating_config ADD COLUMN is_connected INT DEFAULT 0 NOT NULL;
ALTER TABLE extend_rating_config ADD COLUMN nb_vote_per_user INT DEFAULT 0 NOT NULL;
ALTER TABLE extend_rating_config ADD COLUMN delete_vote INT DEFAULT 0 NOT NULL;

--
-- Structure for table extend_rating_vote_history
--
DROP TABLE IF EXISTS extend_rating_vote_history;
CREATE TABLE extend_rating_vote_history (
	id_vote_history INT DEFAULT 0 NOT NULL,
	id_extender_history INT DEFAULT 0 NOT NULL,
	vote_value INT DEFAULT 0 NOT NULL,
	PRIMARY KEY (id_vote_history)
);

UPDATE core_template SET template_value = '<#assign averageScore = 0 />\r\n<#assign voteCount = 0 />\r\n<#if rating??>\r\n	<#assign averageScore = rating.averageScore />\r\n	<#assign voteCount = rating.voteCount />\r\n</#if>\r\n<#if show == \"all\" || show == \"vote\">\r\n	<p>\r\n		<img src=\"images/local/skin/plugins/extend/modules/rating/stars_${averageScore!}.png\" alt=\"Note\" title=\"Note\" />\r\n		(${voteCount!})&nbsp;\r\n	</p>\r\n</#if>\r\n<#if canDeleteVote><div class=\"cancel\"><a href=\"jsp/site/plugins/extend/modules/rating/DoCancelVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType!}" > </a></div></#if>\r\n<#if show == \"all\" || show == \"actionVote\">\r\n	<#if canVote>\r\n		<div> Votez : </div>\r\n		<div class=\"resource-vote-star-rating\" style=\"display:none;\">\r\n			<form name=\"resource_vote_form\" action=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType}\" method=\"post\" >\r\n				<input type=\"hidden\" name=\"voteValue\" value=\"-2\" />\r\n				<input class=\"star-rating\" type=\"radio\" name=\"voteValue_${extendableResourceType}_${idExtendableResource!}\" value=\"-2\" />\r\n				<input class=\"star-rating\" type=\"radio\" name=\"voteValue_${extendableResourceType}_${idExtendableResource!}\" value=\"-1\" />\r\n				<input class=\"star-rating\" type=\"radio\" name=\"voteValue_${extendableResourceType}_${idExtendableResource!}\" value=\"1\" />\r\n				<input class=\"star-rating\" type=\"radio\" name=\"voteValue_${extendableResourceType}_${idExtendableResource!}\" value=\"2\" />\r\n			</form>\r\n		</div>\r\n		<div class=\"resource-vote-star-rating-javascript-disable\" >\r\n			<div class=\"star\">\r\n				<a href=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType!}&voteValue=-2\" ></a>\r\n			</div >\r\n			<div class=\"star\">\r\n				<a href=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType!}&voteValue=-1\" > </a>\r\n			</div>\r\n			<div class=\"star\">\r\n				<a href=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType!}&voteValue=1\" > </a>\r\n			</div>\r\n			<div class=\"star\">\r\n				<a href=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType!}&voteValue=2\" > </a>\r\n			</div>\r\n		</div>\r\n		<br/>\r\n	</#if>\r\n</#if>' WHERE template_name = 'extend_rating_vote_type_star';
UPDATE core_template SET template_value = '\r\n<#assign scoreValue = 0 />\r\n<#assign voteCount = 0 />\r\n<#if rating??>\r\n	<#assign scoreValue = rating.scoreValue />\r\n	<#assign voteCount = rating.voteCount />\r\n</#if>\r\n<#if show == \"all\" || show == \"vote\">\r\n	<p>\r\n		<#if ( scoreValue < 0 )>\r\n			<img src=\"images/local/skin/plugins/extend/modules/rating/vote_against.png\" title=\"#i18n{module.extend.rating.rating.labelVoteAgainst}\" alt=\"#i18n{module.extend.rating.rating.labelVoteAgainst}\"/>\r\n			${scoreValue!}\r\n		<#else>\r\n			<img src=\"images/local/skin/plugins/extend/modules/rating/vote_for.png\" title=\"#i18n{module.extend.rating.rating.labelScore}\" alt=\"#i18n{module.extend.rating.rating.labelScore}\"/>\r\n			${scoreValue!}\r\n		</#if>\r\n		(${voteCount!})&nbsp;\r\n	</p>\r\n</#if>\r\n<#if canDeleteVote><div class=\"cancel\"><a href=\"jsp/site/plugins/extend/modules/rating/DoCancelVote.jsp?idExtendableResource=${idExtendableResource!}&extendableResourceType=${extendableResourceType!}" > </a></div></#if>\r\n<#if show == \"all\" || show == \"actionVote\">\r\n	<#if canVote>\r\n		<div>\r\n			<div class=\"extend-rating-vote-title\">\r\n				<label for=\"\">#i18n{module.extend.rating.rating.labelVote} :</label></div>\r\n				<span class=\"extend-rating-vote-span\">\r\n					<a href=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&amp;extendableResourceType=${extendableResourceType!}&amp;voteValue=-1\">\r\n						<img src=\"images/local/skin/plugins/extend/modules/rating/vote_against.png\" title=\"#i18n{module.extend.rating.rating.labelVoteAgainst}\" alt=\"#i18n{module.extend.rating.rating.labelVoteAgainst}\"/> \r\n					</a>\r\n					<a href=\"jsp/site/plugins/extend/modules/rating/DoVote.jsp?idExtendableResource=${idExtendableResource!}&amp;extendableResourceType=${extendableResourceType!}&amp;voteValue=1\">\r\n						<img src=\"images/local/skin/plugins/extend/modules/rating/vote_for.png\" title=\"#i18n{module.extend.rating.rating.labelVoteFor}\" alt=\"#i18n{module.extend.rating.rating.labelVoteFor}\"/>\r\n					</a>\r\n				</span>\r\n		</div>\r\n	</#if>\r\n</#if>\r\n' WHERE template_name = 'extend_rating_vote_type_thumb';