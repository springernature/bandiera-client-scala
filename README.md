
Bandiera Client (Scala)
=========================

This is a client for talking to the [Bandiera][bandiera] feature flagging service from a Scala application.
This client currently only implements the read methods of the [v2 Bandiera API][bandiera-api].

[![MIT licensed][shield-license]][info-license]


Installation
------------

supported scala versions: 2.11.x, 2.12.x, 2.13.x

in build.sbt add the dependency:
```
"com.springernature" %% "bandiera-client-scala" % "0.3.1"
```




Usage
-----

Create an instance of the bandiera client:

```scala
import com.springernature.bandieraclientscala._

val client = new BandieraClient()
```

Options
-------

you can initialize BandieraClient with:
- baseApiUri: defaults to `http://127.0.0.1:5000/api`
- backend: an `sttp` backend that returns a Future.
  by default we use `AsyncHttpClientFutureBackend`.
  read more here: https://sttp.readthedocs.io/en/latest/backends/summary.html?highlight=scala.concurrent.Future

notice: BandieraClient expects to have an ExecutionContext implicitly available in scope.
- either `import scala.concurrent.ExecutionContext.Implicits.global`
- or provide an ExecutionContext in second constructor params group:
  ``` new BandieraClient(...)(ec = yourExecutor)```


Contributing
------------

If you would like to contribute please make sure that the tests pass and that the code lints successfully.


License
-------

Copyright &copy; 2020 Springer Nature.
Scala Bandiera client is licensed under the [MIT License][info-license].



[bandiera]: https://github.com/nature/bandiera
[bandiera-api]: https://github.com/nature/bandiera/wiki/API-Documentation
[info-license]: LICENSE
[shield-license]: https://img.shields.io/badge/license-MIT-blue.svg

