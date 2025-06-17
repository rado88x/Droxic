# Munro's Tables

A simple web service which supports searching and sorting Munro's tables. Only hills included in the most recent
classification are available to query. Query results are returned as a list of hills, formatted as a JSON document.


## Querying the data

A number of query parameters are available to customize the search criteria. All query parameters are optional and
may be applied in any order. The full list of query parameters is:

* `category=Munro|Top` - Filter the hills by a category, i.e. Munros or Tops. When this parameter is omitted, all hills
  are returned.
* `minHeight` - Specified to the nearest tenth of a metre. Hills will match if they are greater than or equal to the
  minimum height.
* `maxHeight` - Specified to the nearest tenth of a metre. Hills will match if they are less than or equal to the
  maximum height.
* `limit` - Limit the number of hills returned. Must be a positive integer.
* `sort` - Hills may be sorted by `name` or `height` or both. The sort parameter is specified as the name of the field,
  followed by an underscore (`_`) and then the sort order (`asc` or `desc`), e.g. `height_desc`. To sort by both name
  and height, specify two sort parameters in the query string, one for each field. The order of the fields
  determines the final order of the hills, e.g. if sorting by height descending and name ascending, the second sort
  parameter is only used when the heights are equal.
