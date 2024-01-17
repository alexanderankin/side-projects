import { useQuery } from "react-query";
import { AddMoreCitations, JumboTron, LoadingListGroup, okResponse } from "./reusable";
import { formatDate } from "./dateFormatUtils";
import { Link } from "react-router-dom";
import { Citation } from "./models";

export function Citations() {
  let citationsQuery = useQuery({
    queryFn: async () => fetch('/api/citations')
      .then(okResponse)
      .then(r => r.json())
      .then(b => (b as Array<Citation>))
    ,
    // queryKey:
  })

  return <div className='container'>
    <div className='row'>
      <div className='col'>
        <JumboTron title='Citations'>
          <p>List of articles being tracked</p>
          <AddMoreCitations />
        </JumboTron>
      </div>
    </div>
    <div className='row'>
      <div className='col'>
        <LoadingListGroup
          result={citationsQuery}
          keyFn={e => e.id}
          nodeFn={e => <div style={{overflowX: 'auto'}}>
            <Link to={'/citations/' + e.id}>
              {e.name}
            </Link>
            {!!e.description ? <> <span className='italic'>{e.description || ''}</span></> : null}
            <span className='ms-1 small text-muted float-end'>Updated: {formatDate(new Date(e.updated_at))}</span>
            <span className='ms-1 small float-end'>Created: {formatDate(new Date(e.created_at))}</span>
          </div>}
        />
      </div>
    </div>
  </div>
}
