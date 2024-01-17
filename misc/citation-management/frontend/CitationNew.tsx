import { formDataObject, Header } from './Reusable';
import { Button, Col, Form, InputGroup, Row } from 'react-bootstrap';
import { useMutation } from "react-query";
import { okResponse } from "./App";
import { Link } from "react-router-dom";

export function NewCitationPage() {
  return <>
    <Header />
    <NewCitation />
  </>
}

function NewCitation() {
  let newCitationMutation = useMutation('CitationNew.create', async ({ name, description }: { name: string, description?: string, }) =>
    fetch('/api/citations', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({
        name,
        description,
      })
    }).then(okResponse).then(r => r.json()))

  return <div className='container'>
    <div className='row'>
      <div className='col'>
        <p>This page lets you add an article to be referenced as a citation from other articles.</p>
        <p>Enter the article details below:</p>
      </div>
    </div>
    {newCitationMutation.isError
      ? <>
        <div className='row'>
          <div className='col'>
            <div className="alert alert-danger" role="alert">
              Could not create the citation: {String(newCitationMutation.error?.['message'] || newCitationMutation.error)}
            </div>
          </div>
        </div>
        {String(newCitationMutation.error).toLowerCase().includes('unique')
          ? <div className='row'>
            <div className='col'>
              <div className="alert alert-warning" role="alert">
                Citation already exists!
              </div>
            </div>
          </div>
          : null
        }
      </>
      : null
    }
    {newCitationMutation.isSuccess
      ? <div className='row'>
        <div className='col'>
          <div className="alert alert-success" role="alert">
            Created the citation:
            {' '}<Link to={'/citations/' + newCitationMutation.data.id}>
              {newCitationMutation.data.name}
            </Link>
          </div>
        </div>
      </div>
      : null
    }


    <div className='row'>
      {/* example from - https://react-bootstrap.netlify.app/docs/forms/validation/ */}
      <Form validated onSubmit={e => {
        e.preventDefault();
        let object = formDataObject(new FormData(e.target as HTMLFormElement))
        newCitationMutation.mutate(object as { name, description });
      }}>
        <Row className='mb-3'>
          <Form.Group as={Col} md='6' controlId='validationCustom01'>
            <Form.Label>Name</Form.Label>
            <Form.Control
              required
              type='text'
              name='name'
              placeholder='Article title'
              // defaultValue='Mark'
            />
            {/* <Form.Control.Feedback>Looks good!</Form.Control.Feedback> */}
          </Form.Group>
          <Form.Group as={Col} md='6' controlId='validationCustom02'>
            <Form.Label>Description</Form.Label>
            <Form.Control
              // required
              type='text'
              name='description'
              placeholder='Note about the article (optional)'
              // defaultValue='Otto'
            />
            {/* <Form.Control.Feedback>Looks good!</Form.Control.Feedback> */}
          </Form.Group>
        </Row>
        <Button variant='outline-success' type='submit'>Create</Button>
      </Form>
    </div>
  </div>
}
