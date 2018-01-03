# ParkingOS_cloud
<h1>接口源码</h1></br>提供收费员app，车主app的接口调用，第三方的接口api

改为idea项目 maven管理 

<br>
maven 环境配置：<br>
1、修改本地maven环境中的setting.xml文件，把下载地址改为
   <mirror><br>
        <id>local_mirror</id><br>
        <mirrorOf>*</mirrorOf><br>
        <name>local_mirror</name><br>
        <url>http://106.75.7.55:8081/nexus/content/groups/public/</url><br>
    </mirror><br>
	<profiles><br>
		<!-- 在setting中设置私服可以让本机的所有Maven项目都使用Maven私服 -->
		<profile><br>
            <id>nexus</id><br>
            <repositories><br>
                <repository><br>
                    <id>nexus_public</id><br>
                    <url>http://106.75.7.55:8081/nexus/content/groups/public/</url><br>
                   ...<br>
                </snapshots>    <br>
            </repository><br>
			</repositories><br>
			<pluginRepositories><br>
				<pluginRepository><br>
					<id>nexus_public</id><br>
					<url>http://106.75.7.55:8081/nexus/content/groups/public/</url><br>
					  ...<br>
				</pluginRepository><br>
			</pluginRepositories><br>			
		</profile><br>
	</profiles><br>
2、在setting.xml文件中配置本地仓库目录：<localRepository>D:\user\mavenrepo</localRepository><br>
3、项目中的maven环境指向本地的setting.xml文件<br>