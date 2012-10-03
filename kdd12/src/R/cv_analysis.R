library(ggplot2)
results <- read.table(file="/Users/mkokkodi/git/kdd12/cv_data/results/cv_results.csv",head=TRUE,sep=",")
summary(results)

mae_improvement <- (results$MAEBasline-results$MAEModel)/results$MAEBasline
improvements = transform(results,improvement=mae_improvement )
head(improvements)

tmp<-factor(improvements$Score)
summary(tmp)

summary(improvements)

binomial<-improvements[improvements$Model=='Binomial',]
summary(binomial)
ob1 <- ggplot(binomial,aes(binomial$History, binomial$improvement,colour=factor(binomial$Score),shape=factor(binomial$Score))) 
ob1+geom_point(size=5)+geom_line(size = 1.1)+facet_wrap(~Approach,ncol=1)

multinomial<-improvements[improvements$Model=='Multinomial',]
ob1 <- ggplot(multinomial,aes(History, improvement,colour=Approach,shape=Approach)) 
ob1+geom_point(size=5)+geom_line(size = 1.1)

