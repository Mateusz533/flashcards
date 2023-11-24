package pl.mateuszfrejlich.flashcards.dao;

import org.springframework.stereotype.Repository;
import pl.mateuszfrejlich.flashcards.model.Flashcard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository("mySQL")
public class MySQLAdapter implements DataBaseAdapter {
    private static final String SCHEMA_NAME = "flashcard-collection";
    private static final String DB_URL = "jdbc:mysql://localhost/" + SCHEMA_NAME;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "Guest123";
    private static final String PREPARED_CARDS_SUFFIX = "-pre";
    private static final String ARCHIVED_CARDS_SUFFIX = "-arch";
    private static final List<String> SECTION_SUFFIXES = List.of("-sec-1", "-sec-2", "-sec-3", "-sec-4", "-sec-5");
    private Statement statement = null;

    @Override
    public void openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new SQLException("Failed to add MySQL JDBC Driver!");
        }
        Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        statement = connection.createStatement();
    }

    @Override
    public Stream<String> getCollectionNames() {
        List<String> names = new ArrayList<>();
        String query = "SHOW TABLES FROM `" + SCHEMA_NAME + "`;";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                names.add(name);
            }
        } catch (Exception e) {
            return null;
        }

        return names.stream()
                .filter(s -> s.endsWith(PREPARED_CARDS_SUFFIX))
                .map(s -> s.substring(0, s.length() - PREPARED_CARDS_SUFFIX.length()));
    }

    @Override
    public Stream<Flashcard> getPreparedCards(String collectionName) {
        return getCards(collectionName + PREPARED_CARDS_SUFFIX);
    }

    @Override
    public Stream<Flashcard> getArchivedCards(String collectionName) {
        return getCards(collectionName + ARCHIVED_CARDS_SUFFIX);
    }

    @Override
    public List<Stream<Flashcard>> getCardBoxSections(String collectionName) {
        ArrayList<Stream<Flashcard>> sections = new ArrayList<>(5);

        for (String suffix : SECTION_SUFFIXES) {
            Stream<Flashcard> cards = getCards(collectionName + suffix);
            if (cards == null)
                return null;
            else
                sections.add(cards);
        }

        return sections;
    }

    @Override
    public void updatePreparedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream) {
        overrideTable(selectedCollectionName + PREPARED_CARDS_SUFFIX, stream);
    }

    @Override
    public void updateArchivedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream) {
        overrideTable(selectedCollectionName + ARCHIVED_CARDS_SUFFIX, stream);
    }

    @Override
    public void updateInboxCardsCollection(String selectedCollectionName, List<Stream<Flashcard>> sections) {
        for (int i = 0; i < SECTION_SUFFIXES.size(); ++i) {
            overrideTable(selectedCollectionName + SECTION_SUFFIXES.get(i), sections.get(i));
        }
    }

    @Override
    public boolean createNewCollection(String name, Stream<Flashcard> initialData) {
        for (String suffix : allSuffixes()) {
            String query = getCreateTableQuery(name + suffix);
            if (!safeExecute(query))
                return false;
        }

        return (initialData == null) || fillTable(name + PREPARED_CARDS_SUFFIX, initialData);
    }

    @Override
    public boolean deleteCollection(String name) {
        if (name == null)
            return false;

        for (String suffix : allSuffixes()) {
            String query = getDeleteTableQuery(name + suffix);
            if (!safeExecute(query))
                return false;
        }
        return true;
    }

    private String getCreateTableQuery(String name) {
        String query = "CREATE TABLE `" + SCHEMA_NAME + "`.`" + name + "` (\n";
        query += "`id` INT NOT NULL AUTO_INCREMENT,\n";
        query += "`front` VARCHAR(255) NOT NULL,\n";
        query += "`reverse` VARCHAR(255) NOT NULL,\n";
        query += "PRIMARY KEY (`id`),\n";
        query += "UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);\n";

        return query;
    }

    private String getDeleteTableQuery(String name) {
        return "DROP TABLE `" + SCHEMA_NAME + "`.`" + name + "`;";
    }

    private Stream<Flashcard> getCards(String tableName) {
        String query = "SELECT * FROM `" + SCHEMA_NAME + "`.`" + tableName + "`;";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            return getCardsFromResponse(resultSet);
        } catch (Exception e) {
            return null;
        }
    }

    private Stream<Flashcard> getCardsFromResponse(ResultSet resultSet) {
        List<Flashcard> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String front = resultSet.getString(2);
                String reverse = resultSet.getString(3);
                list.add(new Flashcard(front, reverse));
            }
        } catch (Exception e) {
            return null;
        }
        return list.stream();
    }

    private void overrideTable(String name, Stream<Flashcard> stream) {
        String query = "DELETE FROM `" + SCHEMA_NAME + "`.`" + name + "`;";
        if (safeExecute(query))
            fillTable(name, stream);
    }

    private boolean fillTable(String tableName, Stream<Flashcard> stream) {
        String queryBegin = "INSERT INTO `" + SCHEMA_NAME + "`.`" + tableName + "`(front, reverse) VALUES ('";

        return stream.allMatch(card -> {
            String query = queryBegin + card.frontText() + "', '" + card.reverseText() + "');";
            return safeExecute(query);
        });
    }

    private List<String> allSuffixes() {
        List<String> suffixes = new ArrayList<>(SECTION_SUFFIXES);
        suffixes.add(PREPARED_CARDS_SUFFIX);
        suffixes.add(ARCHIVED_CARDS_SUFFIX);

        return suffixes;
    }

    private boolean safeExecute(String query) {
        try {
            statement.execute(query);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
