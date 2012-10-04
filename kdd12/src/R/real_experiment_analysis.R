library(ggplot2)
results <- read.table(file="/Users/mkokkodi/git/kdd12/results/real_experiment_results.csv",head=TRUE,sep=",")
summary(results)


mae_improvement<-(results$MAEBaseline - results$MAEModel)/results$MAEBaseline


improvements = transform(results,improvement=mae_improvement )
head(improvements)




binomial<-improvements[improvements$Model=='Binomial',]
summary(binomial)
ob1 <- ggplot(binomial,aes(History, improvement,colour=factor(Score),shape=factor(Score))) 
ob1+geom_point(size=5)+geom_line(size = 1.1)+facet_wrap(~Approach,ncol=1)

multinomial<-improvements[improvements$Model=='Multinomial',]
ob1 <- ggplot(multinomial,aes(History, improvement,colour=Approach,shape=Approach)) 
ob1+geom_point(size=5)+geom_line(size = 1.1)
