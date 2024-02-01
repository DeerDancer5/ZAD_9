package pl.edu.pb.zad_9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fetchBooksData("");
    }

    private class BookHolder extends RecyclerView.ViewHolder {

        private static final String IMG_URL_BASE = "https://covers.openlibrary.org/b/id/";
        private TextView bookTitle;
        private TextView bookAuthor;
        private TextView numberOfPages;
        private ImageView bookCover;

        public BookHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.book_list_item,parent,false));

            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            numberOfPages = itemView.findViewById(R.id.number_of_pages);
            bookCover = itemView.findViewById(R.id.img_cover);
        }

        public void bind(Book book) {
            if(book!=null && checkNullOrEmpty(book.getTitle()) && book.getAuthors() != null) {
                bookTitle.setText(book.getTitle());
                bookAuthor.setText(TextUtils.join(", ",book.getAuthors()));
                numberOfPages.setText(book.getNumberOfPages());

                if(book.getCover()!=null) {
                    Picasso.with(itemView.getContext())
                            .load(IMG_URL_BASE + book.getCover() + "-S.jpg")
                            .placeholder(R.drawable.baseline_book_24).into(bookCover);
                }
                else {
                    bookCover.setImageResource(R.drawable.baseline_book_24);
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.book_menu,menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                fetchBooksData(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void fetchBooksData(String query) {
        String finalQuery = prepareQuery(query);
        BookService bookService = RetrofitInstance.getRetrofitInstance().create(BookService.class);

        Call<BookContainer> booksApiCall = bookService.findBooks(finalQuery);

        booksApiCall.enqueue(new Callback<BookContainer>() {
            @Override
            public void onResponse(Call<BookContainer> call, Response<BookContainer> response) {
                if(response.body() != null) {
                    setupBookListView(response.body().getBookList());
                }
            }

            @Override
            public void onFailure(Call<BookContainer> call, Throwable t) {
                Snackbar.make(findViewById(R.id.recyclerView),"Something went wrong",
                        BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    private String prepareQuery(String query) {
        String[] queryParts = query.split("\\s+");
        return TextUtils.join("+",queryParts);
    }

    private void setupBookListView(List<Book> books) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final BookAdapter adapter = new BookAdapter();
        adapter.setBooks(books);
        String message = "Books: "+books.size();
        Log.d("MainActivity",message);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean checkNullOrEmpty(String text) {
        return text != null && !TextUtils.isEmpty(text);
    }

    private class BookAdapter extends RecyclerView.Adapter<BookHolder> {
        private List <Book> books;


        @NonNull
        @Override
        public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookHolder(getLayoutInflater(),parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BookHolder holder, int position) {
            if(books!=null) {
                Book book = books.get(position);
                holder.bind(book);
            }
            else {
                Log.d("MainActivity","No books");
            }
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
        void setBooks(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }
    }
}