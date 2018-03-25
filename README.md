# Jobcoin Mixer
1. You provide a list of new, unused addresses that you own to the mixer;
2. The mixer provides you with a new deposit address that it owns;
3. You transfer your jobcoins to that address in the [Jobcoin network](https://jobcoin.gemini.com/vendetta);
4. The mixer will detect your transfer by watching or polling the P2P [Jobcoin network](https://jobcoin.gemini.com/vendetta);
5. The mixer will transfer your jobcoin from the deposit address into a big “house account” along with all the other bitcoin currently being mixed; and
6. Then, over some time the mixer will use the house account to dole out your jobcoins in smaller increments to the withdrawal addresses that you provided, possibly after deducting a fee.

## Ops

[SBT](https://www.scala-sbt.org/) is required for below instructions but binaries w/ launcher script can be sent if preferred.

***To Test***: 

```
jobcoin-mixer> sbt test
```

***To Run***:

```
jobcoin-mixer> sbt stage
jobcoin-mixer> target/universal/stage/bin/jobcoin-mixer
```
```
service URL: POST: localhost:9000/jobcoin-mixer/addresses
request: {"proxyRecipients": ["newAddress1", "newAddress2"]}
response: {"depositAddress": "depositAddress1"}
```
## Notes
- Days are shortened to minutes
