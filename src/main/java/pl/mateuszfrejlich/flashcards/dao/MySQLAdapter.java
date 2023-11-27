package pl.mateuszfrejlich.flashcards.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Repository("mySQL")
public class MySQLAdapter implements DataBaseAdapter {
    private static final String PREPARED_CARDS_SUFFIX = "-pre";
    private static final String ARCHIVED_CARDS_SUFFIX = "-arch";
    private static final List<String> SECTION_SUFFIXES = List.of("-sec-1", "-sec-2", "-sec-3", "-sec-4", "-sec-5");
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void openConnection() throws SQLException {
        try {
            Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
        } catch (SQLException | NullPointerException e) {
            throw new SQLException("Failed to connect with database!");
        }
    }

    @Override
    public Stream<String> getCollectionNames() {
        List<String> names = jdbcTemplate.query("SHOW TABLES;", (rs, rowNum) -> rs.getString(1));
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
    public boolean updatePreparedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream) {
        return overrideTable(selectedCollectionName + PREPARED_CARDS_SUFFIX, stream);
    }

    @Override
    public boolean updateArchivedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream) {
        return overrideTable(selectedCollectionName + ARCHIVED_CARDS_SUFFIX, stream);
    }

    @Override
    public boolean updateInboxCardsCollection(String selectedCollectionName, List<Stream<Flashcard>> sections) {
        for (int i = 0; i < SECTION_SUFFIXES.size(); ++i) {
            if (!overrideTable(selectedCollectionName + SECTION_SUFFIXES.get(i), sections.get(i)))
                return false;
        }
        return true;
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
            String query = "DROP TABLE `" + name + suffix + "`;";
            if (!safeExecute(query))
                return false;
        }
        return true;
    }

    private Stream<Flashcard> getCards(String tableName) {
        try {
            String query = "SELECT `front`, `reverse` FROM `" + tableName + "`;";
            List<Flashcard> list = jdbcTemplate.query(query, (rs, rowNum) -> {
                String front = rs.getString("front");
                String reverse = rs.getString("reverse");
                return new Flashcard(front, reverse);
            });
            return list.stream();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean overrideTable(String name, Stream<Flashcard> stream) {
        String query = "DELETE FROM `" + name + "`;";
        return safeExecute(query) && fillTable(name, stream);
    }

    private boolean fillTable(String tableName, Stream<Flashcard> stream) {
        if (stream == null)
            return true;

        String queryBegin = "INSERT INTO `" + tableName + "`(`front`, `reverse`) VALUES\n";
        String values = stream.map(card -> "('" + card.frontText() + "', '" + card.reverseText() + "'),")
                .reduce("", (a, b) -> a + b);
        if (values.isEmpty())
            return true;

        String query = queryBegin + values.substring(0, values.length() - 1) + ";";

        return safeExecute(query);
    }

    private boolean safeExecute(String query) {
        try {
            jdbcTemplate.execute(query);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    private List<String> allSuffixes() {
        List<String> suffixes = new ArrayList<>(SECTION_SUFFIXES);
        suffixes.add(PREPARED_CARDS_SUFFIX);
        suffixes.add(ARCHIVED_CARDS_SUFFIX);

        return suffixes;
    }

    private String getCreateTableQuery(String name) {
        return "CREATE TABLE `" + name + "` (\n" + """
                `id` INT NOT NULL AUTO_INCREMENT,
                `front` VARCHAR(255) NOT NULL,
                `reverse` VARCHAR(255) NOT NULL,
                PRIMARY KEY (`id`),
                UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE)""";
    }
}

