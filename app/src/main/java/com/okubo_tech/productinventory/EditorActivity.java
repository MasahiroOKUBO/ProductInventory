package com.okubo_tech.productinventory;

/**
 * Created by okubo on 2016/12/10.
 * <p>
 * editor activity class.
 */

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.okubo_tech.productinventory.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;


public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Member
     */
    private final static String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private EditText mSupplierEmailEditText;
    private EditText mDescriptionEditText;
    private Spinner mTemperatureSpinner;
    private ImageView mImageImageView;

    private Button mOrderButton;
    private Button mCameraButton;

    private int mTemperature = ProductEntry.TEMPERATURE_NOMAL;
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    /**
     * Override
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        /** New or Edit**/
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        /** Find views **/
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_product_supplier_email);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mTemperatureSpinner = (Spinner) findViewById(R.id.edit_product_temperature_spinner);
        mImageImageView = (ImageView) findViewById(R.id.product_image);
        mOrderButton = (Button) findViewById(R.id.ordre_button);
        mCameraButton = (Button) findViewById(R.id.camera_button);

        /** set listener **/
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mTemperatureSpinner.setOnTouchListener(mTouchListener);
        mImageImageView.setOnTouchListener(mTouchListener);



        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditorActivity.this, "mOrderButton", Toast.LENGTH_SHORT).show();
                String productSupplierEmail = mSupplierEmailEditText.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + productSupplierEmail));
                intent.putExtra(Intent.EXTRA_SUBJECT, "ORDER");
                intent.putExtra(Intent.EXTRA_TEXT, "some message here.");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditorActivity.this, "mImageImageView", Toast.LENGTH_SHORT).show();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        /** set Spinner **/
        setupSpinner();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageImageView.setImageBitmap(imageBitmap);
            saveProduct();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_TEMPERATURE,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            /** index **/
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int temperatureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_TEMPERATURE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            /** value **/
            String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            String productSupplier = cursor.getString(supplierColumnIndex);
            String productSupplierEmail = cursor.getString(supplierEmailColumnIndex);
            String productDescription = cursor.getString(descriptionColumnIndex);
            int productTemperature = cursor.getInt(temperatureColumnIndex);
            byte[] productImageByte = cursor.getBlob(imageColumnIndex);

            /** set value **/
            mNameEditText.setText(productName);
            mPriceEditText.setText(String.valueOf(productPrice));
            mQuantityEditText.setText(String.valueOf(productQuantity));
            mSupplierEditText.setText(productSupplier);
            mSupplierEmailEditText.setText(productSupplierEmail);
            mDescriptionEditText.setText(productDescription);

            switch (productTemperature) {
                case ProductEntry.TEMPERATURE_COLD:
                    mTemperatureSpinner.setSelection(1);
                    break;
                case ProductEntry.TEMPERATURE_FREEZE:
                    mTemperatureSpinner.setSelection(2);
                    break;
                default:
                    mTemperatureSpinner.setSelection(0);
                    break;
            }

            // if image exist, set image. else set def image.
            Bitmap productImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_store);
            if (productImageByte != null){
                productImage = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
            }
            mImageImageView.setImageBitmap(productImage);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierEmailEditText.setText("");
        mDescriptionEditText.setText("");
        mTemperatureSpinner.setSelection(0);
        Bitmap productImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_store);
        mImageImageView.setImageBitmap(productImage);
    }

    /**
     * Helper
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteEntry();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteEntry() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_entry_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_entry_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void setupSpinner() {
        ArrayAdapter temperatureAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_temperature_options, android.R.layout.simple_spinner_item);

        temperatureAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mTemperatureSpinner.setAdapter(temperatureAdapter);
        mTemperatureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.temperature_cold))) {
                        mTemperature = ProductEntry.TEMPERATURE_COLD;
                    } else if (selection.equals(getString(R.string.temperature_freeze))) {
                        mTemperature = ProductEntry.TEMPERATURE_FREEZE;
                    } else {
                        mTemperature = ProductEntry.TEMPERATURE_NOMAL;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTemperature = ProductEntry.TEMPERATURE_NOMAL;
            }
        });
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        Bitmap imageBitmap = ((BitmapDrawable)mImageImageView.getDrawable()).getBitmap();

        /** sanity check **/
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, "Not Saved. Require Name, Price, Quantity, Email !!", Toast.LENGTH_LONG).show();
            return;
        }

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();

        /** Create a ContentValues object **/
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductEntry.COLUMN_PRODUCT_TEMPERATURE, mTemperature);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, bytes);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_entry_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_entry_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_entry_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_entry_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
