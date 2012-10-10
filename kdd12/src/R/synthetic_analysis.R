library(ggplot2)
results <- read.table(file="/Users/mkokkodi/git/kdd12/results/synthetic_results.csv",head=TRUE,sep=",")
summary(results)

head(results)
mae_improvement<-(results$MAEBaseline -results$MAEModel)/results$MAEBaseline
improvements = transform(results,improvement=mae_improvement )
head(improvements)

synlabels = c("3 categories","5 categories","7 categories","Baseline")
ob1 <- ggplot(improvements,aes(History, improvement*100,colour=factor(Categories),shape=factor(Categories))) 
ob1+geom_point(size=3)+geom_line(size = 1.1)+facet_wrap(~Approach * Score,ncol=4) + theme_bw(18) +
  xlab(expression(History - eta))+
  ylab("Improvement %")+scale_shape_discrete(labels=synlabels)+ 
  scale_colour_discrete(labels=synlabels)+labs(colour="",shape="")+
  theme(legend.position = "top",axis.title.y = element_text(vjust=-0.1),axis.title.x = element_text(vjust=-0.3),plot.margin = unit(c(1, 1, 1, 1), "cm"))

ggsave(file="/Users/mkokkodi/Documents/workspace/reputation_informs/figures/synthetic.pdf",width=17,height=6,dpi=300)

hier <- read.table(file="/Users/mkokkodi/git/kdd12/results/synthetic_hier_results.csv",head=TRUE,sep=",")
summary(hier)
mae_improvement<-(hier$MAEBaseline -hier$MAEModel )/hier$MAEBaseline
hier_improvements = transform(hier,improvement=mae_improvement )
hier_improvements
summary(hier_improvements)

hierlabels=c(expression(theta==0.5),expression(theta==0.6)," Baseline")
ob1 <- ggplot(hier_improvements,aes(History, improvement*100,colour=factor(Score),shape=factor(Score))) 
ob1+geom_point(size=5)+geom_line(size = 1)+facet_wrap(~Approach,ncol=2)+theme_bw(22) + xlab(expression(History - eta))+
  ylab("Improvement %")+scale_shape_discrete(labels=hierlabels)+ 
  scale_colour_discrete(labels=hierlabels)+labs(colour="",shape="")+
  theme(axis.title.y = element_text(vjust=-0.1),axis.title.x = element_text(vjust=-0.3),plot.margin = unit(c(1, 1, 1, 1), "cm"))

ggsave(file="/Users/mkokkodi/Documents/workspace/reputation_informs/figures/syntheticHier.pdf",width=15,height=5,dpi=300)

