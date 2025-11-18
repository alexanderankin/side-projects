import Paper from "@mui/material/Paper";
import {
  DataGrid,
  type GridColDef,
  type GridDataSource,
  type GridGetRowsParams,
  type GridGetRowsResponse, type GridPaginationModel, type GridUpdateRowParams,
} from "@mui/x-data-grid";

import classNames from "classnames";
import { useState } from "react";
import { Link, Outlet } from "react-router-dom";


export function App() {
  const [navbarNavCollapsed, setNavbarNavCollapsed] = useState(true);
  let collapseNavBar = setNavbarNavCollapsed.bind(null, true);

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
        <div className="container-fluid">
          <Link className="navbar-brand" to="/">Mc Management</Link>
          <button
            className="navbar-toggler"
            type="button"
            onClick={() => setNavbarNavCollapsed((s) => !s)}
            aria-controls="navbarNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <div className={classNames({ "navbar-collapse": true, collapse: navbarNavCollapsed })}>
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <Link className="nav-link" to="/versions" onClick={collapseNavBar}>Versions</Link>
              </li>
              {/*<li className="nav-item">
                <Link className="nav-link" to="/play" onClick={collapseNavBar}>Play</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/settings" onClick={collapseNavBar}>Settings</Link>
              </li>*/}
            </ul>
          </div>
        </div>
      </nav>

      <div className="sidebar">
        <ul className="nav flex-column">
          <li className="nav-item">
            <Link className="nav-link" to="/versions" onClick={collapseNavBar}>Versions</Link>
          </li>
          {/*<li className="nav-item">
            <Link className="nav-link" to="/play" onClick={collapseNavBar}>Play</Link>
          </li>
          <li className="nav-item">
            <Link className="nav-link" to="/settings" onClick={collapseNavBar}>Settings</Link>
          </li>*/}
        </ul>
      </div>

      <div className="main-content">
        <Outlet />
      </div>
    </>
  );
}

export function Welcome() {
  return <>
    <h1>Welcome</h1>
    <p>Welcome</p>
  </>;
}

export function Versions() {
  return <>
    <h1>Versions</h1>
    <p>Versions</p>

    <DataTable />
  </>;
}


const columns: GridColDef[] = [
  { field: "id", headerName: "ID", width: 70 },
  { field: "firstName", headerName: "First name", width: 130 },
  { field: "lastName", headerName: "Last name", width: 130 },
  {
    field: "age",
    headerName: "Age",
    type: "number",
    width: 90,
  },
  {
    field: "fullName",
    headerName: "Full name",
    description: "This column has a value getter and is not sortable.",
    sortable: false,
    width: 160,
    valueGetter: (value, row) => `${row.firstName || ""} ${row.lastName || ""}`,
  },
];

const rows = [
  { id: 1, lastName: "Snow", firstName: "Jon", age: 35 },
  { id: 2, lastName: "Lannister", firstName: "Cersei", age: 42 },
  { id: 3, lastName: "Lannister", firstName: "Jaime", age: 45 },
  { id: 4, lastName: "Stark", firstName: "Arya", age: 16 },
  { id: 5, lastName: "Targaryen", firstName: "Daenerys", age: null },
  { id: 6, lastName: "Melisandre", firstName: null, age: 150 },
  { id: 7, lastName: "Clifford", firstName: "Ferrara", age: 44 },
  { id: 8, lastName: "Frances", firstName: "Rossini", age: 36 },
  { id: 9, lastName: "Roxie", firstName: "Harvey", age: 65 },
];

const paginationModel: GridPaginationModel = { page: 0, pageSize: 5 };

/*
* export interface GridDataSource {
    getRows(params: GridGetRowsParams): Promise<GridGetRowsResponse>
    updateRow?(params: GridUpdateRowParams): Promise<any>
}
*/

class MyDs implements GridDataSource {
  getRows(params: GridGetRowsParams): Promise<GridGetRowsResponse> {
    params.paginationModel
    return Promise.resolve({
      rows: [],
      rowCount: 0,
      pageInfo: {
        hasNextPage: true,
        nextCursor: "pointer",
      }
    });
  }

  updateRow(params: GridUpdateRowParams): Promise<any> {
    if (params.previousRow === params.updatedRow)
      return Promise.resolve()


    return Promise.resolve(undefined);
  }

}

export default function DataTable() {
  return (
    <Paper sx={{ height: 400, width: "100%" }}>
      <DataGrid
        // rows={rows}
        dataSource={new MyDs()}
        columns={columns}
        initialState={{ pagination: { paginationModel } }}
        pageSizeOptions={[5, 10]}
        checkboxSelection
        sx={{ border: 0 }}
      />
    </Paper>
  );
}
