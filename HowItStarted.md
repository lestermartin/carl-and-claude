# How it all started

Carl is still living in the 80's (an incredible decade, but not the one we are living in) and his customers are running away from him because he REQUIRES a personal touch to do ANYTHING.

Carl is NOT a programmer and he surely is not a Generative AI expert, but he has played around with ChatGPT the last year or three and has been reading all of these impossible sounding stories on LinkedIn. Stories of people building things that historically they needed to get help to create due to the lack of technical 'chops'. 

On a whim, Carl decided to see if he could be one of this non-technical 'vibe programmers' and one of his clients suggested he try Claude. He got all set up with some basical tools and he took a few minutes to jot down what he thought made sense (ok, with a little help from his technical buddy, Lester) to initially give to Claude.

Here is what they threw over the wall...

```
Carl needs brokerage self-service tools. He is NOT a programmer, but he heard about "vibe coding" with Claude and he gave it a go!

let me tell you a bit about the project I want to create. ultimately, it is a self-service brokerage web application (front-end implemented with Angular). Initially, we can start with only 9 users in the system (usernames of customer1 through customer9) who all have a simple password of 'cu$tP@$$w0rd'. Each should have a random first & last name, tax ID number, and USA-based home address.

customers should have access to a configurable set of stock exchanges and initially configured to offer Nasdaq, NYSE, Shanghai Stock Exchange, and the London Stock Exchange. this will allow them to hold a portfolio of stocks from the available exchanges.

In addition to a log in/out page, an edit profile page is needed where they can change any value except their username or password. Their homepage should show a summary of their stock holdings. 

They also need to be able to put in a simple buy or sell order that only offer market order or limit order choices. Ideally a real market data API is desired, but initially cost is a concern. An acceptable initial compromise would be to see a list of the top 100 stocks from each of the supported exchanges as the other options available for purchase.

Each of the initial customers should have a portfolio that includes a $40,000 cash balance and a stock portfolio of 3-10 holdings selected at random from the available stocks for purchase. In additional to their current portfolio, their buy/sell transaction log should be maintained. Randomly select dates within the last 3 years for the purchase dates and if possible, use the historical market close price. For each of the random 3-10 purchases for each user, purchase somewhere between $20,000 to $40,000 worth of stocks. 

The Angular web app should communicate with REST services that are implemented with Java & Spring. Initially, the implementations of buy & sell functionality can be very simply with not exhaustive business rules beyond making sure the customer has enough money in their cash balance. Some initial unit tests should be created for the actual Java code.

Again, recent stock market prices are acceptable as the cost of the market data API is a concern. If possible, use publicly available resources that are free, or offer limited use for limited costs. 

The Java tier will access and persist data in a postgresql database. The database name should be 'tradingapp' with user 'db-user' and password 'db-p@$$w0rd'.

For a runtime environment, I want to deploy & run the comprehensive application stack via docker compose on my local machine. I already have docker installed and only need the appropriate configuration files and instruction on how to bring the containers up and down. I will also need to expose ports to my localhost so that I can see the web application in my web brower and access postgresql on its default port. 

I want the comprehensive application to exist in this single github repo and include overarching instructions of how to build and deploy the full app.
```

And, well... the rest is the evolving story of Carl & Claude (& Lester keeping them honest) iterating on Carl's new online trading application.