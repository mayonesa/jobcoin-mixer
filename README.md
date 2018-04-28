# Jobcoin Mixer

## Use
1. Provide a list of new, unused addresses that you own to the mixer;
2. The mixer provides a new deposit address that it owns;
3. Transfer your jobcoins to that address in the [Jobcoin network](https://jobcoin.gemini.com/vendetta);
4. The mixer will detect a transfer by watching or polling the P2P [Jobcoin network](https://jobcoin.gemini.com/vendetta);
5. The mixer will transfer jobcoins from the deposit address into a series of “house accounts” along with all the other jobcoin currently being mixed; and
6. Then, over some time the mixer will use the house account to dole out your jobcoins in smaller increments to the withdrawal addresses that were provided in step #1.

## Ops

[SBT](https://www.scala-sbt.org/) is required.

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
- T is in terms of minutes
