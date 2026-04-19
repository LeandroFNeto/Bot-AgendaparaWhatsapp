# depedência da ia no pom.xml

````xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
````
### adicionamos

Logica na classe ServicoIA que ate entao so tinha um esboço base, basicamente quando o cliente nao seguir a ideia padrao 
do menu e digitar algo fora do comum ai entramos com a ia com casos mais fora do comum, decidimos por hora trabalha com
gemini 1.5 flash. 

Para deixar o .env melhor de definido deixei um example no git
