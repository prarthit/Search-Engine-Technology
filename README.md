### Steps to run the program:

1. Modify the ***config.properties*** file according to needs(find property parameteres below for more info).
2. Enter the search query. (The relevant search result documents would be displayed)
3. The program will ask for if you want to view any document (Select a document to view (y/n): ). Type 'y' if you want to see the document contents, else 'n'
    - If you press 'y', for the next input enter the document name without the filePath (e.g - 18.json)
4. Repeat step 2.

> ***NOTE:*** Enter Biword queries with quotes, Phrase queries with quotes and Near queries with square brackets. See examples below

### Query Examples

- **Basic query**: whale
- **Phrase query**: "fires in yosemite"
- **Near query**: [national near/2 park]
- **Wildcard query**: m\*k\*d
- **AND query**: park organization
- **OR query**: bear + hiking
- **Boolean query**: bear + park organization + "fires in y\*mi\*e" + hiking

### config.properties

`corpus_directory_path`: The path for corpus directory which contains documents

`resources_dir`: Directory path where disk indexes should be stored

`token_processor`: Type of token processor
- `BASIC` for basic token processor which only cleans whitespaces from queries  
- *(default)* `ADVANCED` for advanced token processor which cleans whitespaces, performs stemming on queries and tokenization on hyphens

`query_mode`: Mode of the query
- *(default)* `BOOLEAN` for running queries in boolean mode(e.g. Phrase query, Near query, AND query, OR query, Basic query etc.)
- `RANKED` for running queries in ranked mode with bag of words terminology (only supports Basic and Prase queries)