TrackSearch-ContentProvider

TrackSearch-ContentProvider is an Android Content Provider implementation that allows apps to store and retrieve search queries efficiently. This provider enables other apps to access the stored search history when requested, ensuring seamless data sharing.

Features

Store search queries efficiently.

Retrieve search history when required.

Provide data access to other apps via Content Resolver.

How Content Providers Work

A Content Provider in Android acts as an abstraction layer for data access, enabling apps to share and manage data securely. The TrackSearch-ContentProvider stores search queries and makes them accessible through a URI-based system.

URI Structure

The Content Provider uses a predefined URI format:

content://com.mshetty.tracksearch.search.searchhistorydatabase/history

Supported Operations

Insert a Search Query

To insert a new search query into the provider:

ContentValues values = new ContentValues();
values.put("query", "Your search query");
getContentResolver().insert(Uri.parse("content://com.yourpackage.provider/search"), values);

Retrieve Search Queries

To query the stored search history:

           val cursor: Cursor = getContentResolver().query(
                Uri.parse("content://com.mshetty.tracksearch.search.searchhistorydatabase/history"),
                null, null, null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val query = cursor.getString(cursor.getColumnIndex("query"))
                    Log.d("SearchQuery", query)
                }
                cursor.close()
            }

Delete Search Queries

To delete stored search queries:

getContentResolver().delete(Uri.parse("content://com.mshetty.tracksearch.search.searchhistorydatabase/history"), null, null);

How Other Apps Can Use This Data

Other applications can interact with this Content Provider using the same URI and standard ContentResolver methods. Ensure that the provider is exported and accessible in your AndroidManifest.xml:

<provider
    android:name=".search.db.HistoryProvider"
    android:authorities="com.mshetty.tracksearch.search.searchhistorydatabase"
    android:exported="true" />

This allows external apps to fetch search history data, making integration seamless.
[Screen_recording_20250110_164900.webm](https://github.com/user-attachments/assets/87552dfe-1ec6-4b58-9020-af35356bf165)
