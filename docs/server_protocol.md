# API Protocol

Simple api protocol for Full Line Code Completion
- [Server status](#server-status)
- [List of languages](#list-of-languages)
- [Complete code](#complete-code) 
    - [Parameters](#parameters)
    - [Request](#request)

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
1. 500 (If exception pop up)