<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="/">
		<failures>
			<xsl:for-each select="/root/compositeResponse[httpStatusCode != '200' and httpStatusCode != '201']">
				<failure>
					<referenceId>
						<xsl:value-of select="referenceId"/>
					</referenceId>
					<httpStatusCode>
						<xsl:value-of select="httpStatusCode"/>
					</httpStatusCode>
					<errorCode>
						<xsl:value-of select="body/errorCode"/>
					</errorCode>
					<message>
						<xsl:value-of select="body/message"/>
					</message>
					<fields>
						<xsl:value-of select="body/fields"/>
					</fields>
				</failure>
			</xsl:for-each>
		</failures>
	</xsl:template>
</xsl:stylesheet>