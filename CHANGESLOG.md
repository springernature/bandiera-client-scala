Changes log
===========

## 0.3.1 2020-05-07

- dependency updates: improve ranges to better avoid binary incompatibilities
- update sbt plugin to 1.3.10
- update build plugins: sbt-sonatype and sbt-pgp


## 0.3.0 2019-09-24

- scala 2.13 support
- json: replace play-json with com.lihaoyi.upickle
- update sbt plugin to 1.2.8
- update build plugins: sbt-sonatype and sbt-pgp


## 0.2.0 2018-08-31

- add support for missing bandiera errors:
  - UserIdMissing: when user_id is expected for a feature flag request (percentage based)
  - UserGroupMissing: when user_group is expected for a feature flag request (group based)
  - MultipleWarning: when requesting multiple feature flags and user_id or user_group is needed for some
- nest test cases
- use custom uTest framework
  - lower nicer format nested cases in summary to 5
  - set wider line width (140 chars) before wrapping


## 0.1.2

- make sttp backend a main constructor arg and remove the 5 second timeout option


## 0.1.1

- cross compile for scala versions 2.11 & 2.12
- improve readme

## 0.1.0

- initial release to maven
