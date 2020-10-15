package word_ranking;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class Handler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {

		// request reading
		HashMap event;
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			event = gson.fromJson(reader, HashMap.class);
		} catch (JsonSyntaxException ignored) {
			event = new HashMap();
		}
		// extract string to recognize from request
		String sentence;
		if (event.containsKey("sentence")) {
			sentence = (String)event.get("sentence");
		} else {
			returnResult(outputStream, false);
			return;
		}
		String rankingTableName;
		if (event.containsKey("ranking_table_name")) {
			rankingTableName = (String)event.get("ranking_table_name");
		} else {
			returnResult(outputStream, false);
			return;
		}

		// prepare Dynamo connection
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(client);

		// isolate words
		Matcher matcher = Pattern.compile("[a-zA-Z]+").matcher(sentence);
		while (matcher.find()) {
			rankWord(dynamoDB, matcher.group(), rankingTableName);
		}

		// return response
		returnResult(outputStream, true);
	}

	private static void rankWord(DynamoDB connection, String word, String tableName) {

		// prepare and perform request
		Table table = connection.getTable(tableName);
		UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("word", word.toLowerCase())
				.withUpdateExpression("ADD word_counter :word_counter")
				.withValueMap(new ValueMap().withNumber(":word_counter", 1))
				.withReturnValues("NONE");
		table.updateItem(updateItemSpec);

	}

	private static void returnResult(@NotNull OutputStream outputStream, boolean ok) {

		// response creation
		String result;
		if (ok) {
			result = "Updated";
		} else {
			result = "Error";
		}

		// response writing
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)));
		writer.write(result);
		writer.close();
	}
}
