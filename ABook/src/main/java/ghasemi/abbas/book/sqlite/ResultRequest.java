/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.sqlite;

import java.util.List;

public interface ResultRequest {

    void onSuccess(List<SqlModel> sqlModels);

    void onFail();

}
