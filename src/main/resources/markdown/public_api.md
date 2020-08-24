## Public HTTP API

### Prepare Transaction 

POST https://convex.world/api/v1/transaction/prepare

**Payload**
 ```json
{
  "address": "0c43f0c4-8652-405d-ab5b-b08496d08621",
  "source": "(map inc [1 2 3])"
}
```

**Response**
 ```json
{
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "source": "(map inc [1 2 3])"
}
```

### Submit Transaction

POST https://convex.world/api/v1/transaction/submit

**Payload**
 ```json
{
  "address": "0c43f0c4-8652-405d-ab5b-b08496d08621",
  "hash": "badb861fc51d49e0212c0304b1890da42e4a4b54228986be17de8d7dccd845e2",
  "sig": "ce31365976f0c5a5922f65f47999907f5fab475cce1fdad0ff53baaf800036a4ed1783b6dbb98b14a25e1bfffd140749223f6914b86533e6fa9811de0733cc0b"
}
```

**Response**

 ```json
{
  "id": 1,
  "value": [1, 2, 3]
}
```

The `error-code` key is present if the response is an error:

 ```json
{
  "id": 18,
  "value": "Can't convert 1 to class class convex.core.data.ASequence",
  "error-code": "CAST"
}
```