package com.ruoyi.generator.util;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import com.ruoyi.common.core.controller.BaseController;

import java.util.Collections;

/**
 * @author admin
 * @Date 2024/9/8 2:08
 */
public class GeneratorUtil {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://114.132.162.232:3306/zhifubao?serverTimezone=UTC", "root", "aa258258")
                .dataSourceConfig(builder -> builder
                        .dbQuery(new MySqlQuery())
                        .schema("mybatis-plus")
                        .typeConvert(new MySqlTypeConvert())
                        .keyWordsHandler(new MySqlKeyWordsHandler())
                        .databaseQueryClass(SQLQuery.class)
                        .build())
                .globalConfig(builder -> builder
                        .author("admin")
                        .outputDir("/Users/zhouchaolong/IdeaProjects/ctpay/ruoyi-system/src/main/java")
                        //设置注释日期格式
                        .commentDate("yyyy-MM-dd")
                        .disableOpenDir()
                        //开启swagger模式
                        .enableSwagger()
                        // 设置时间类型策略
                        .dateType(DateType.ONLY_DATE)
                        .build()
                )
                .packageConfig(builder -> builder
                        .parent("com.ruoyi.system")
                        .entity("domain.business")
                        .service("service.business")
                        .serviceImpl("service.business.impl")
                        .mapper("mapper.business")
                        .xml("business")
                        .controller("controller")
                        // 设置路径配置信息
                        .pathInfo(Collections.singletonMap(OutputFile.xml, "/Users/zhouchaolong/IdeaProjects/ctpay/ruoyi-system/src/main/resources/mapper/business"))
                        .build()
                )
                .strategyConfig(builder -> builder
                        .entityBuilder()
                        //禁用生成 serialVersionUID
                        .disableSerialVersionUID()
                        //开启 Lombok 模型
                        .enableLombok()
                        //逻辑删除字段名
                        .logicDeleteColumnName("del_flag")
                        //逻辑删除属性名
                        .logicDeletePropertyName("delFlag")
                        //数据库表映射到实体的命名策略
                        .naming(NamingStrategy.underline_to_camel)
                        //数据库表字段映射到实体的命名策略
                        .columnNaming(NamingStrategy.underline_to_camel)
                        //添加表字段填充
                        .addTableFills(new Column("create_time", FieldFill.INSERT))
                        .addTableFills(new Property("updateTime", FieldFill.INSERT_UPDATE))
                        .formatFileName("%sEntity")
                        .mapperBuilder()
                        .superClass(BaseMapper.class)
                        .enableBaseColumnList()
                        .enableBaseResultMap()
                        .formatMapperFileName("%sMapper")
                        .formatXmlFileName("%sMapper")
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImp")
                        .controllerBuilder()
                        .superClass(BaseController.class)
                        .build()
                )
                .strategyConfig(builder -> builder
                        .addInclude("ct_req_order")
                        .addTablePrefix("ct_")
                        .build()).execute();
    }
}
