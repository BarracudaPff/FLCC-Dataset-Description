Creating scalable service for completion on python.

## Main packages:
 - **application** - Used for framework-dependency code
 - **domain** - Used for dedicated domain code, wich sadly can be transport 
 - **infrastructure** - Used for talking with another services or calling framework-dependence code from domain part. Also need for ml's library code, so we can easily replace them

So the structure is
<pre>  
Action ─> Service ─> Connector
                  └─> Generator
</pre>

## Definitions:
 - **Action** - Here we get json data, values from headers, Service instance. Action must validate data, map into object and send to Service's method
 - **Service** - Top level of domain part, get only valid values (not Business Logic). Mast check for bl errors and perform result, call fetcher or connector.
 - **Connector** - High level connector for ML. Here you are free to use ml's libraries

## Also
Every `exception` | `bl-error` | `validation` | ... must be thrown up to an application level, where every custom exception (except of Runtime) must throw `ApiException` based on cause.

## Issues
closes #23 
