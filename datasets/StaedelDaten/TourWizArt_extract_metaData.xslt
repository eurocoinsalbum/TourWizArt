<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="text" omit-xml-declaration="yes"/>
 <xsl:strip-space elements="*"/>

<xsl:template match="/">
	<xsl:for-each select="/adlibXML/recordList/record">
		<xsl:text>"</xsl:text>
		<xsl:value-of select="priref"/>
		<xsl:text>";"</xsl:text>
		<xsl:value-of select="Production_date/production.date.start"/>
		<xsl:text>";"</xsl:text>
		<xsl:value-of select="Production_date/production.date.end"/>
		<xsl:text>";"</xsl:text>
		<xsl:value-of select="school_style/term[@lang='de-DE']"/>
		<xsl:text>";"</xsl:text>
		<xsl:value-of select="Technique/technique/term[@lang='de-DE']"/>
		<xsl:text>"&#xd;&#xa;</xsl:text>
	</xsl:for-each>
</xsl:template>
</xsl:stylesheet>
