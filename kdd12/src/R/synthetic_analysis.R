library(ggplot2)
results <- read.table(file="/Users/mkokkodi/git/kdd12/results/synthetic_results.csv",head=TRUE,sep=",")
summary(results)

head(results)
mae_improvement<-(results$MAEBaseline -results$MAEModel)/results$MAEBaseline
improvements = transform(results,improvement=mae_improvement )
head(improvements)


ob1 <- ggplot(improvements,aes(History, improvement,colour=factor(Categories),shape=factor(Categories))) 
ob1+geom_point(size=3)+geom_line(size = 1.1)+facet_wrap(~Approach * Score,ncol=4)

hier <- read.table(file="/Users/mkokkodi/git/kdd12/results/synthetic_hier_results.csv",head=TRUE,sep=",")
summary(hier)
mae_improvement<-(hier$MAEBaseline -hier$MAEModel )/hier$MAEBaseline
hier_improvements = transform(hier,improvement=mae_improvement )
hier_improvements
summary(hier_improvements)

ob1 <- ggplot(hier_improvements,aes(History, improvement,colour=factor(Score),shape=factor(Score))) 
ob1+geom_point(size=3)+geom_line(size = 1.1)+facet_wrap(~Approach,ncol=4)
