# 一个小脚本工程(试验)

## 涉及的技术

技术点:

* HTTP 请求: [apache http components]


## 生成独立 jar 包

执行命令: 

```
mvn package assembly:single
```

则, 在target目录下生成一个xxx-jar-with-dependencies.jar文件，这个文件不但包含了自己项目中的代码和资源，
还包含了所有依赖包的内容, 所以可以直接通过java -jar来运行.






[apache http components]:       http://hc.apache.org/