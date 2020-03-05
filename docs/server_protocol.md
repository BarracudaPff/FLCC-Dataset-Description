# API Protocol

Simple api protocol for Full Line Code Completion
- [Server status](#server-status)
- [Completion Models](#completion-models)
    - [List of models](#list-of-models)
    - [Load existing or new model](#load-existing-or-new-model)
    - [Release current model](#release-current-model)
- [List of languages](#list-of-languages)
- [Complete code](#complete-code) 
    - [Parameters](#parameters)
    - [Request](#request)

## Completion Models
All models named according to the pattern: `<type>-<name>`

### List of models
#### GET `/v1/models?type=all`
Get list of current loaded models<br/>

#### Parameters
| Parameter  | Type                                                                 | Description               |
| ---------- |:--------------------------------------------------------------------:| :-------------------------|
| type       | string (supported values: `main`, `custom`, `all`; default `all`)    | Type of completion model  |

**Response:**
```json
{
  "models": [
    // 'main' models: if model labled as main, it will be always available
    {
      "model": "gpt-checkpoint0",
      "main": true,
      "best": true // one of main models will be marked as 'best' (used as default for completion) 
    },
    // 'custom' models: if model not labled as main, it has lifetime
    {
      "model": "gpt-checkpoint0",
      "main": false,
      "start_at": 1583420504,
      "time_left": 60000
    }
  ]
}
```
**Returns:**
1. 200 
1. 400 (If there were errors in the request)
1. 500 (If exception pop up)

### Load existing or new model
#### POST `/v1/models/load`
Get list of current loaded models<br/>

#### Parameters
| Parameter     | Type                                                          | Description                                                       |
| ----------    |:--------------------------------------------------------:     | :-----------------------------------------------------------------|
| type          | string (supported values: `gpt`, `txl` - Transformer-XL)      | Model type, `gpt` - GPT-2 and `txl`- Transformer-XL               |
| model         | string                                                        | Model name, note that final name will be `<type>-<name>`          |
| lifetime      | int (default `60`)                                            | Model's lifetime in *seconds*, maximum value is 3600 (one hour).  |
| url           | string (default `null`)                                       | Model's url to download,                                          |
| data-url      | string (default `null`)                                       | Pass If model need some additional data                           |
| new-dataset   | boolean (default `false`)                                     | Set true if model use new normalized dataset                      |

#### Request
```json
{
  "type": "gpt",
  "model": "checkpoint0",
  "lifetime": 60
}
```

**Response:**
```json
{
  "model": {
    "model": "gpt-checkpoint0",
    "main": false,
    "start_at": 1583420504,
    "time_left": 60000
  }
}
```
**Returns:**
1. 200
1. 400 (If there were errors in the request)
1. 404 (In case of loading non existing model with url)
1. 500 (If exception pop up)

### Release current model
#### GET `/v1/models/release?model=gpt-checkpoint0`
Unload current model

#### Parameters
| Parameter  | Type                                                                 | Description               |
| ---------- |:--------------------------------------------------------------------:| :-------------------------|
| model      | string | Model name, must be one of currently loaded. Note that you can't release main models   |

**Response:**
```json
{
  "success": true
}
```
**Returns:**
1. 200 
1. 400 (If there were errors in the request)
1. 404 (In case of releasing non existing or non loaded model)
1. 500 (If exception pop up)

### Server status
#### GET `/v1/status`
Check server status

**Response:**
```json
{
  "status": true
}
```
**Returns:**
1. 200 
1. 500 (If exception pop up)

##
### List of languages
#### GET `/v1/languages`
**Successful Response**
Get list of all supported languages

```json
{
  "languages": [
    "Python3"
  ]
}
```
**Unsuccessful Response**
```json
{
  "error": {
    "code": 500,
    "issue": {
      "message": "<EXCEPTION_MESSAGE>"
    },
    "message": "<API_EXCEPTION_CLASS>",
    "type": "<EXCEPTION_CLASS>"
  }
}
```

**Returns:**
1. 200 
1. 500 (If exception pop up)

##

### Complete code
#### POST `/v1/complete?language=LANGUAGE`
Get completions based on current model, settings and code

#### Parameters
| Parameter             | Type                                                  | Description                                                                                                               |
| --------------------- |:-----------------------------------------------------:| :-------------------------------------------------------------------------------------------------------------------------|
| code                  | string                                                | Codebase used to complete (full context)                                                                                  |
| prefix                | string                                                | Last token before space or another symbol, used for correct completion in IDEA (ex. for `find(va⎮)` must return `va`)     |
| offset                | int                                                   | Cursor's offset when called completion                                                                                    |
| filename              | string                                                | Filename (ex. `test.py`)                                                                                                  |
| mode                  | string (supported values: `ONE_TOKEN`, `FULL_LINE`)   | Completion mode                                                                                                           |
| model                 | string (default `best`)                               | Completion model. Must be in model list. If such model not loaded, automatically load it with default lifetime            |
| num_iterations        | int (default `10`)                                    |                                                                                                                           |
| beam_size             | int (default `3`)                                     |                                                                                                                           |
| diversity_groups      | int (default `1`)                                     |                                                                                                                           |
| diversity_strength    | double (default `0.3`)                                |                                                                                                                           |
| top_n                 | int (default `5`)                                     | Pass it if you want to set maximum amount of suggestions, otherwise set null                                              |
| only_full_lines       | boolean (default `true`)                              | When set to true, returns only completed full lines                                                                       |
#### Request
```json
{
  "code": "import num\nimport os",
  "prefix": "num",
  "offset": 10,
  "filename": "test.py",
  "mode": "FULL_LINE",
  "num_iterations": 10,
  "beam_size": 3,
  "diversity_groups": 3,
  "diversity_strength": 0.3,
  "top_n": 5,
  "only_full_lines": true
}
```
**Successful Response**
```json
{
  "completions": [
    "numpy as np",
    "numpy",
    "numpy # np"
  ]
}
```
**Unsuccessful Response**
```json
{
  "error": {
    "code": 500,
    "issue": {
      "message": "<EXCEPTION_MESSAGE>"
    },
    "message": "<API_EXCEPTION_CLASS>",
    "type": "<EXCEPTION_CLASS>"
  }
}
```
**Returns:**
1. 200 
1. 204 (If request was canceled by server)
1. 400 (If there were errors in the request)
1. 500 (If exception pop up)
